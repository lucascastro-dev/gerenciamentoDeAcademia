package gerenciamentoDeAcademia.servicos.colaborador;

import gerenciamentoDeAcademia.dto.CriarSolicitacaoFeriasRequest;
import gerenciamentoDeAcademia.dto.DecidirSolicitacaoFeriasRequest;
import gerenciamentoDeAcademia.dto.PeriodoAquisitivoFeriasDto;
import gerenciamentoDeAcademia.dto.ResumoFeriasColaboradorDto;
import gerenciamentoDeAcademia.dto.SolicitacaoFeriasDto;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.SolicitacaoFerias;
import gerenciamentoDeAcademia.entidades.VinculoFuncionarioInstituicao;
import gerenciamentoDeAcademia.enums.StatusSolicitacaoFerias;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.SolicitacaoFeriasRepository;
import gerenciamentoDeAcademia.repositorios.VinculoFuncionarioInstituicaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ServicoFerias {

    public static final int DIAS_DIREITO = 30;
    public static final int PERIODO_AQUISITIVO_DIAS = 365;

    private static final Set<StatusSolicitacaoFerias> STATUS_CONSUMEM_SALDO = EnumSet.of(
            StatusSolicitacaoFerias.PENDENTE,
            StatusSolicitacaoFerias.APROVADO);

    private final SolicitacaoFeriasRepository solicitacaoRepository;
    private final VinculoFuncionarioInstituicaoRepository vinculoRepository;
    private final InstituicaoRepository instituicaoRepository;

    @Transactional(readOnly = true)
    public ResumoFeriasColaboradorDto resumoColaborador(Long instituicaoId, String cpfColaborador) {
        validarCpf(cpfColaborador);
        VinculoFuncionarioInstituicao vinculo = buscarVinculo(instituicaoId, cpfColaborador);
        LocalDate admissao = resolverDataAdmissao(vinculo);
        List<SolicitacaoFerias> solicitacoes = solicitacaoRepository
                .findByInstituicao_IdAndCpfColaboradorOrderByCriadoEmDesc(instituicaoId, cpfColaborador);
        List<PeriodoAquisitivoFeriasDto> periodos = montarPeriodos(admissao, LocalDate.now(), solicitacoes);

        int disponiveis = periodos.stream().mapToInt(PeriodoAquisitivoFeriasDto::diasDisponiveis).sum();
        int aprovados = solicitacoes.stream()
                .filter(s -> s.getStatus() == StatusSolicitacaoFerias.APROVADO)
                .mapToInt(SolicitacaoFerias::getDiasSolicitados)
                .sum();
        int pendentes = solicitacoes.stream()
                .filter(s -> s.getStatus() == StatusSolicitacaoFerias.PENDENTE)
                .mapToInt(SolicitacaoFerias::getDiasSolicitados)
                .sum();

        return new ResumoFeriasColaboradorDto(
                disponiveis,
                aprovados,
                pendentes,
                periodos,
                solicitacoes.stream().map(SolicitacaoFeriasDto::of).toList());
    }

    @Transactional
    public SolicitacaoFeriasDto solicitar(
            Long instituicaoId, String cpfColaborador, String nomeColaborador,
            CriarSolicitacaoFeriasRequest request) {
        validarCpf(cpfColaborador);
        ExcecaoDeDominio.quandoNulo(request, "Informe o período desejado.");
        ExcecaoDeDominio.quandoNulo(request.getDataInicio(), "Informe a data de início.");
        ExcecaoDeDominio.quandoNulo(request.getDataFim(), "Informe a data de fim.");
        ExcecaoDeDominio.quando(request.getDataFim().isBefore(request.getDataInicio()),
                "A data fim deve ser igual ou posterior à data de início.");

        long dias = ChronoUnit.DAYS.between(request.getDataInicio(), request.getDataFim()) + 1;
        ExcecaoDeDominio.quando(dias <= 0 || dias > DIAS_DIREITO,
                "Informe um período entre 1 e " + DIAS_DIREITO + " dias.");

        VinculoFuncionarioInstituicao vinculo = buscarVinculo(instituicaoId, cpfColaborador);
        LocalDate admissao = resolverDataAdmissao(vinculo);
        List<SolicitacaoFerias> existentes = solicitacaoRepository
                .findByInstituicao_IdAndCpfColaboradorOrderByCriadoEmDesc(instituicaoId, cpfColaborador);

        PeriodoAquisitivoFeriasDto periodo = localizarPeriodoParaDatas(
                admissao, request.getDataInicio(), existentes);
        ExcecaoDeDominio.quando(periodo == null,
                "As datas informadas não pertencem a um período aquisitivo com saldo disponível.");
        ExcecaoDeDominio.quando(periodo.diasDisponiveis() < dias,
                "Saldo insuficiente neste período aquisitivo. Disponível: "
                        + periodo.diasDisponiveis() + " dia(s).");

        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));

        SolicitacaoFerias solicitacao = SolicitacaoFerias.builder()
                .instituicao(instituicao)
                .cpfColaborador(cpfColaborador)
                .nomeColaborador(nomeColaborador)
                .dataInicio(request.getDataInicio())
                .dataFim(request.getDataFim())
                .diasSolicitados((int) dias)
                .inicioPeriodoAquisitivo(periodo.inicio())
                .fimPeriodoAquisitivo(periodo.fim())
                .status(StatusSolicitacaoFerias.PENDENTE)
                .criadoEm(LocalDateTime.now())
                .build();

        return SolicitacaoFeriasDto.of(solicitacaoRepository.save(solicitacao));
    }

    @Transactional
    public SolicitacaoFeriasDto cancelar(Long instituicaoId, String cpfColaborador, Long solicitacaoId) {
        SolicitacaoFerias solicitacao = buscarSolicitacao(instituicaoId, solicitacaoId);
        ExcecaoDeDominio.quando(!solicitacao.getCpfColaborador().equals(cpfColaborador),
                "Solicitação não pertence ao colaborador.");
        ExcecaoDeDominio.quando(solicitacao.getStatus() != StatusSolicitacaoFerias.PENDENTE,
                "Somente solicitações pendentes podem ser canceladas.");
        solicitacao.setStatus(StatusSolicitacaoFerias.CANCELADO);
        return SolicitacaoFeriasDto.of(solicitacaoRepository.save(solicitacao));
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoFeriasDto> listarRh(Long instituicaoId, StatusSolicitacaoFerias status) {
        List<SolicitacaoFerias> lista = status != null
                ? solicitacaoRepository.findByInstituicao_IdAndStatusOrderByCriadoEmDesc(instituicaoId, status)
                : solicitacaoRepository.findByInstituicao_IdOrderByCriadoEmDesc(instituicaoId);
        return lista.stream().map(SolicitacaoFeriasDto::of).toList();
    }

    @Transactional
    public SolicitacaoFeriasDto decidirRh(
            Long instituicaoId, String cpfRh, Long solicitacaoId, DecidirSolicitacaoFeriasRequest request) {
        ExcecaoDeDominio.quandoNulo(request, "Informe a decisão.");
        StatusSolicitacaoFerias novoStatus = request.getStatus();
        ExcecaoDeDominio.quando(
                novoStatus != StatusSolicitacaoFerias.APROVADO && novoStatus != StatusSolicitacaoFerias.REJEITADO,
                "Decisão inválida. Use APROVADO ou REJEITADO.");

        SolicitacaoFerias solicitacao = buscarSolicitacao(instituicaoId, solicitacaoId);
        ExcecaoDeDominio.quando(solicitacao.getStatus() != StatusSolicitacaoFerias.PENDENTE,
                "Esta solicitação já foi analisada.");

        if (novoStatus == StatusSolicitacaoFerias.APROVADO) {
            VinculoFuncionarioInstituicao vinculo = buscarVinculo(instituicaoId, solicitacao.getCpfColaborador());
            LocalDate admissao = resolverDataAdmissao(vinculo);
            List<SolicitacaoFerias> existentes = solicitacaoRepository
                    .findByInstituicao_IdAndCpfColaboradorOrderByCriadoEmDesc(
                            instituicaoId, solicitacao.getCpfColaborador());
            PeriodoAquisitivoFeriasDto periodo = localizarPeriodoPorInicio(
                    admissao, solicitacao.getInicioPeriodoAquisitivo(), existentes, solicitacao.getId());
            ExcecaoDeDominio.quando(periodo == null || periodo.diasDisponiveis() < solicitacao.getDiasSolicitados(),
                    "Saldo insuficiente para aprovar esta solicitação.");
        }

        solicitacao.setStatus(novoStatus);
        solicitacao.setDecididoEm(LocalDateTime.now());
        solicitacao.setDecididoPorCpf(cpfRh != null ? cpfRh.replaceAll("\\D", "") : null);
        solicitacao.setObservacaoRh(request.getObservacaoRh());
        return SolicitacaoFeriasDto.of(solicitacaoRepository.save(solicitacao));
    }

    private SolicitacaoFerias buscarSolicitacao(Long instituicaoId, Long id) {
        return solicitacaoRepository.findById(id)
                .filter(s -> s.getInstituicao().getId().equals(instituicaoId))
                .orElseThrow(() -> new ExcecaoDeDominio("Solicitação de férias não encontrada."));
    }

    private VinculoFuncionarioInstituicao buscarVinculo(Long instituicaoId, String cpf) {
        return vinculoRepository.findByFuncionarioCpfAndInstituicaoId(cpf, instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Vínculo com a instituição não encontrado."));
    }

    LocalDate resolverDataAdmissao(VinculoFuncionarioInstituicao vinculo) {
        if (vinculo.getDataAdmissao() != null) {
            return vinculo.getDataAdmissao();
        }
        return LocalDate.now().minusDays(PERIODO_AQUISITIVO_DIAS - 1L);
    }

    private List<PeriodoAquisitivoFeriasDto> montarPeriodos(
            LocalDate admissao, LocalDate referencia, List<SolicitacaoFerias> solicitacoes) {
        List<PeriodoAquisitivoFeriasDto> periodos = new ArrayList<>();
        LocalDate inicio = admissao;
        int limite = 0;
        while (limite < 6) {
            LocalDate fim = inicio.plusDays(PERIODO_AQUISITIVO_DIAS - 1L);
            int utilizados = diasConsumidos(solicitacoes, inicio, null);
            int pendentes = diasPorStatus(solicitacoes, inicio, StatusSolicitacaoFerias.PENDENTE, null);
            int aprovados = diasPorStatus(solicitacoes, inicio, StatusSolicitacaoFerias.APROVADO, null);
            int disponiveis = calcularDisponiveis(referencia, inicio, fim, utilizados);
            periodos.add(new PeriodoAquisitivoFeriasDto(
                    inicio,
                    fim,
                    DIAS_DIREITO,
                    aprovados,
                    pendentes,
                    disponiveis,
                    situacaoPeriodo(referencia, inicio, fim, disponiveis)));
            if (fim.isAfter(referencia.plusDays(PERIODO_AQUISITIVO_DIAS))) {
                break;
            }
            inicio = inicio.plusDays(PERIODO_AQUISITIVO_DIAS);
            limite++;
        }
        return periodos;
    }

    private PeriodoAquisitivoFeriasDto localizarPeriodoParaDatas(
            LocalDate admissao, LocalDate dataInicio, List<SolicitacaoFerias> solicitacoes) {
        LocalDate referencia = LocalDate.now();
        LocalDate inicio = admissao;
        while (!inicio.isAfter(dataInicio.plusDays(PERIODO_AQUISITIVO_DIAS))) {
            LocalDate fim = inicio.plusDays(PERIODO_AQUISITIVO_DIAS - 1L);
            if (!dataInicio.isBefore(inicio) && !dataInicio.isAfter(fim)) {
                int utilizados = diasConsumidos(solicitacoes, inicio, null);
                int disponiveis = calcularDisponiveis(referencia, inicio, fim, utilizados);
                if (disponiveis > 0) {
                    int aprovados = diasPorStatus(solicitacoes, inicio, StatusSolicitacaoFerias.APROVADO, null);
                    int pendentes = diasPorStatus(solicitacoes, inicio, StatusSolicitacaoFerias.PENDENTE, null);
                    return new PeriodoAquisitivoFeriasDto(
                            inicio, fim, DIAS_DIREITO, aprovados, pendentes, disponiveis,
                            situacaoPeriodo(referencia, inicio, fim, disponiveis));
                }
                return null;
            }
            inicio = inicio.plusDays(PERIODO_AQUISITIVO_DIAS);
        }
        return null;
    }

    private PeriodoAquisitivoFeriasDto localizarPeriodoPorInicio(
            LocalDate admissao, LocalDate inicioPeriodo, List<SolicitacaoFerias> solicitacoes, Long ignorarId) {
        LocalDate referencia = LocalDate.now();
        LocalDate inicio = admissao;
        while (!inicio.isAfter(inicioPeriodo.plusDays(PERIODO_AQUISITIVO_DIAS))) {
            if (inicio.equals(inicioPeriodo)) {
                LocalDate fim = inicio.plusDays(PERIODO_AQUISITIVO_DIAS - 1L);
                int utilizados = diasConsumidos(solicitacoes, inicio, ignorarId);
                int disponiveis = calcularDisponiveis(referencia, inicio, fim, utilizados);
                int aprovados = diasPorStatus(solicitacoes, inicio, StatusSolicitacaoFerias.APROVADO, ignorarId);
                int pendentes = diasPorStatus(solicitacoes, inicio, StatusSolicitacaoFerias.PENDENTE, ignorarId);
                return new PeriodoAquisitivoFeriasDto(
                        inicio, fim, DIAS_DIREITO, aprovados, pendentes, disponiveis,
                        situacaoPeriodo(referencia, inicio, fim, disponiveis));
            }
            inicio = inicio.plusDays(PERIODO_AQUISITIVO_DIAS);
        }
        return null;
    }

    private int calcularDisponiveis(LocalDate referencia, LocalDate inicio, LocalDate fim, int utilizados) {
        if (referencia.isBefore(fim)) {
            return 0;
        }
        return Math.max(0, DIAS_DIREITO - utilizados);
    }

    private String situacaoPeriodo(LocalDate referencia, LocalDate inicio, LocalDate fim, int disponiveis) {
        if (referencia.isBefore(inicio)) {
            return "Futuro";
        }
        if (!referencia.isBefore(inicio) && !referencia.isAfter(fim)) {
            return "Em aquisição";
        }
        if (disponiveis > 0) {
            return "Disponível";
        }
        return "Utilizado";
    }

    private int diasConsumidos(List<SolicitacaoFerias> solicitacoes, LocalDate inicioPeriodo, Long ignorarId) {
        return solicitacoes.stream()
                .filter(s -> ignorarId == null || !s.getId().equals(ignorarId))
                .filter(s -> STATUS_CONSUMEM_SALDO.contains(s.getStatus()))
                .filter(s -> s.getInicioPeriodoAquisitivo().equals(inicioPeriodo))
                .mapToInt(SolicitacaoFerias::getDiasSolicitados)
                .sum();
    }

    private int diasPorStatus(
            List<SolicitacaoFerias> solicitacoes, LocalDate inicioPeriodo,
            StatusSolicitacaoFerias status, Long ignorarId) {
        return solicitacoes.stream()
                .filter(s -> ignorarId == null || !s.getId().equals(ignorarId))
                .filter(s -> s.getStatus() == status)
                .filter(s -> s.getInicioPeriodoAquisitivo().equals(inicioPeriodo))
                .mapToInt(SolicitacaoFerias::getDiasSolicitados)
                .sum();
    }

    private void validarCpf(String cpf) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF do colaborador inválido.");
    }
}
