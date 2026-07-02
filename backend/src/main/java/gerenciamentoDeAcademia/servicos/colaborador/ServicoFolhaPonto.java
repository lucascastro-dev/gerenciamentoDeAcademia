package gerenciamentoDeAcademia.servicos.colaborador;

import gerenciamentoDeAcademia.dto.DecidirAjustePontoFormDto;
import gerenciamentoDeAcademia.dto.FolhaPontoColaboradorRhDto;
import gerenciamentoDeAcademia.dto.RegistroDiaPontoDto;
import gerenciamentoDeAcademia.dto.ResumoPontoMensalDto;
import gerenciamentoDeAcademia.dto.SolicitacaoAjustePontoDto;
import gerenciamentoDeAcademia.dto.SolicitarAjustePontoFormDto;
import gerenciamentoDeAcademia.dto.StatusIntegracaoPontoDto;
import gerenciamentoDeAcademia.dto.StatusPontoHojeDto;
import gerenciamentoDeAcademia.entidades.ConferenciaPontoMensal;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.RegistroDiaPonto;
import gerenciamentoDeAcademia.entidades.SolicitacaoAjustePonto;
import gerenciamentoDeAcademia.entidades.VinculoFuncionarioInstituicao;
import gerenciamentoDeAcademia.enums.StatusSolicitacaoAjustePonto;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.ConferenciaPontoMensalRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.RegistroDiaPontoRepository;
import gerenciamentoDeAcademia.repositorios.SolicitacaoAjustePontoRepository;
import gerenciamentoDeAcademia.repositorios.VinculoFuncionarioInstituicaoRepository;
import gerenciamentoDeAcademia.util.FolhaPontoUtil;
import gerenciamentoDeAcademia.util.RelogioAplicacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServicoFolhaPonto {

    private final RegistroDiaPontoRepository registroRepository;
    private final ConferenciaPontoMensalRepository conferenciaRepository;
    private final VinculoFuncionarioInstituicaoRepository vinculoRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final SolicitacaoAjustePontoRepository ajustePontoRepository;

    @Transactional(readOnly = true)
    public StatusPontoHojeDto statusHoje(Long instituicaoId, String cpfColaborador) {
        validarCpf(cpfColaborador);
        LocalDate hoje = RelogioAplicacao.hoje();
        Optional<RegistroDiaPonto> registro = registroRepository
                .findByInstituicao_IdAndCpfColaboradorAndDataRegistro(instituicaoId, cpfColaborador, hoje);

        if (registro.isEmpty() || registro.get().getHoraEntrada() == null) {
            return new StatusPontoHojeDto(
                    "ENTRADA",
                    null,
                    null,
                    "Registre sua entrada para iniciar o expediente.");
        }
        if (registro.get().getHoraSaida() == null) {
            return new StatusPontoHojeDto(
                    "SAIDA",
                    registro.get().getHoraEntrada(),
                    null,
                    "Entrada registrada. Registre a saída ao encerrar o dia.");
        }
        return new StatusPontoHojeDto(
                "COMPLETO",
                registro.get().getHoraEntrada(),
                registro.get().getHoraSaida(),
                "Entrada e saída já registradas hoje.");
    }

    @Transactional
    public StatusPontoHojeDto marcarPonto(Long instituicaoId, String cpfColaborador, String nomeColaborador) {
        validarCpf(cpfColaborador);
        ExcecaoDeDominio.quandoNuloOuVazio(nomeColaborador, "Nome do colaborador não identificado.");

        if (isMesConferido(instituicaoId, RelogioAplicacao.hoje().getMonthValue(), RelogioAplicacao.hoje().getYear())) {
            throw new ExcecaoDeDominio("O ponto deste mês já foi conferido pelo RH. Novas marcações estão bloqueadas.");
        }

        validarLiberacaoMarcacao(instituicaoId, RelogioAplicacao.hoje());

        LocalDate hoje = RelogioAplicacao.hoje();
        LocalDateTime agora = RelogioAplicacao.agora();

        RegistroDiaPonto registro = registroRepository
                .findByInstituicao_IdAndCpfColaboradorAndDataRegistro(instituicaoId, cpfColaborador, hoje)
                .orElseGet(() -> {
                    Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                            .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));
                    return RegistroDiaPonto.builder()
                            .instituicao(instituicao)
                            .cpfColaborador(cpfColaborador)
                            .nomeColaborador(nomeColaborador)
                            .dataRegistro(hoje)
                            .build();
                });

        registro.setNomeColaborador(nomeColaborador);

        if (registro.getHoraEntrada() == null) {
            registro.setHoraEntrada(agora);
            registroRepository.save(registro);
            return new StatusPontoHojeDto(
                    "SAIDA",
                    registro.getHoraEntrada(),
                    null,
                    "Entrada registrada. Registre a saída ao encerrar o dia.");
        }
        if (registro.getHoraSaida() == null) {
            ExcecaoDeDominio.quando(agora.isBefore(registro.getHoraEntrada()),
                    "Horário de saída não pode ser anterior à entrada.");
            registro.setHoraSaida(agora);
            registroRepository.save(registro);
            return new StatusPontoHojeDto(
                    "COMPLETO",
                    registro.getHoraEntrada(),
                    registro.getHoraSaida(),
                    "Entrada e saída já registradas hoje.");
        }
        throw new ExcecaoDeDominio("Entrada e saída já registradas para hoje.");
    }

    @Transactional(readOnly = true)
    public ResumoPontoMensalDto meuResumoMensal(Long instituicaoId, String cpfColaborador, Integer mes, Integer ano) {
        validarCompetencia(mes, ano);
        validarCpf(cpfColaborador);
        YearMonth competencia = YearMonth.of(ano, mes);
        List<RegistroDiaPonto> registros = registroRepository
                .findByInstituicao_IdAndCpfColaboradorAndDataRegistroBetweenOrderByDataRegistroAsc(
                        instituicaoId,
                        cpfColaborador,
                        competencia.atDay(1),
                        competencia.atEndOfMonth());

        return montarResumo(mes, ano, registros);
    }

    @Transactional(readOnly = true)
    public List<FolhaPontoColaboradorRhDto> listarColaboradoresRh(Long instituicaoId, Integer mes, Integer ano) {
        validarCompetencia(mes, ano);
        YearMonth competencia = YearMonth.of(ano, mes);
        LocalDate inicio = competencia.atDay(1);
        LocalDate fim = competencia.atEndOfMonth();

        List<RegistroDiaPonto> todos = registroRepository
                .findByInstituicao_IdAndDataRegistroBetweenOrderByNomeColaboradorAscDataRegistroAsc(
                        instituicaoId, inicio, fim);

        Map<String, AgregadoPonto> agregados = new HashMap<>();
        for (RegistroDiaPonto registro : todos) {
            agregados.computeIfAbsent(registro.getCpfColaborador(), cpf -> new AgregadoPonto())
                    .acumular(registro);
        }

        return vinculoRepository.findByInstituicaoIdComDetalhes(instituicaoId).stream()
                .filter(v -> v.getFuncionario() != null && Boolean.TRUE.equals(v.getFuncionario().getCadastroAtivo()))
                .map(v -> {
                    Funcionario f = v.getFuncionario();
                    AgregadoPonto ag = agregados.getOrDefault(f.getCpf(), new AgregadoPonto());
                    return new FolhaPontoColaboradorRhDto(
                            f.getCpf(),
                            f.getNome(),
                            v.getTipoFuncionario() != null ? v.getTipoFuncionario().getDescricao() : "Colaborador",
                            ag.diasCompletos,
                            ag.minutos,
                            FolhaPontoUtil.formatarHoras(ag.minutos),
                            ag.aberto);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumoPontoMensalDto detalheColaboradorRh(
            Long instituicaoId, String cpfColaborador, Integer mes, Integer ano) {
        validarCompetencia(mes, ano);
        validarCpf(cpfColaborador);
        YearMonth competencia = YearMonth.of(ano, mes);
        List<RegistroDiaPonto> registros = registroRepository
                .findByInstituicao_IdAndCpfColaboradorAndDataRegistroBetweenOrderByDataRegistroAsc(
                        instituicaoId,
                        cpfColaborador,
                        competencia.atDay(1),
                        competencia.atEndOfMonth());
        return montarResumo(mes, ano, registros);
    }

    @Transactional
    public StatusIntegracaoPontoDto conferirMesRh(Long instituicaoId, String cpfConferidor, Integer mes, Integer ano) {
        validarCompetencia(mes, ano);
        validarCpf(cpfConferidor);
        validarCompetenciaEncerrada(mes, ano);

        List<FolhaPontoColaboradorRhDto> colaboradores = listarColaboradoresRh(instituicaoId, mes, ano);
        boolean possuiAberto = colaboradores.stream().anyMatch(FolhaPontoColaboradorRhDto::possuiRegistroAberto);
        ExcecaoDeDominio.quando(possuiAberto,
                "Existem registros em aberto (entrada sem saída). Regularize antes de conferir.");

        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));

        ConferenciaPontoMensal conferencia = conferenciaRepository
                .findByInstituicao_IdAndMesCompetenciaAndAnoCompetencia(instituicaoId, mes, ano)
                .orElse(ConferenciaPontoMensal.builder()
                        .instituicao(instituicao)
                        .mesCompetencia(mes)
                        .anoCompetencia(ano)
                        .build());

        conferencia.setConferidoEm(RelogioAplicacao.agora());
        conferencia.setConferidoPorCpf(cpfConferidor);
        conferenciaRepository.save(conferencia);

        return statusIntegracao(instituicaoId, mes, ano);
    }

    @Transactional
    public StatusIntegracaoPontoDto reabrirConferenciaRh(Long instituicaoId, Integer mes, Integer ano) {
        validarCompetencia(mes, ano);
        ConferenciaPontoMensal conferencia = conferenciaRepository
                .findByInstituicao_IdAndMesCompetenciaAndAnoCompetencia(instituicaoId, mes, ano)
                .orElseThrow(() -> new ExcecaoDeDominio("Esta competência não está conferida."));

        ExcecaoDeDominio.quando(conferencia.getIntegradoFinanceiroEm() != null,
                "Não é possível reabrir após integração com o financeiro.");

        conferenciaRepository.delete(conferencia);
        return statusIntegracao(instituicaoId, mes, ano);
    }

    @Transactional
    public StatusIntegracaoPontoDto integrarFinanceiro(
            Long instituicaoId, String cpfIntegrador, Integer mes, Integer ano) {
        validarCompetencia(mes, ano);
        ExcecaoDeDominio.quando(!isMesConferido(instituicaoId, mes, ano),
                "O RH ainda não conferiu a folha de ponto deste mês.");

        ConferenciaPontoMensal conferencia = conferenciaRepository
                .findByInstituicao_IdAndMesCompetenciaAndAnoCompetencia(instituicaoId, mes, ano)
                .orElseThrow(() -> new ExcecaoDeDominio("Conferência de ponto não encontrada."));

        conferencia.setIntegradoFinanceiroEm(RelogioAplicacao.agora());
        conferencia.setIntegradoPorCpf(cpfIntegrador != null ? cpfIntegrador.replaceAll("\\D", "") : null);
        conferenciaRepository.save(conferencia);

        return statusIntegracao(instituicaoId, mes, ano);
    }

    @Transactional(readOnly = true)
    public StatusIntegracaoPontoDto statusIntegracao(Long instituicaoId, Integer mes, Integer ano) {
        validarCompetencia(mes, ano);
        Optional<ConferenciaPontoMensal> conferencia = conferenciaRepository
                .findByInstituicao_IdAndMesCompetenciaAndAnoCompetencia(instituicaoId, mes, ano);

        List<FolhaPontoColaboradorRhDto> colaboradores = listarColaboradoresRh(instituicaoId, mes, ano);
        int comRegistro = (int) colaboradores.stream()
                .filter(c -> c.diasTrabalhados() > 0 || c.possuiRegistroAberto())
                .count();
        long totalMinutos = colaboradores.stream()
                .mapToLong(c -> c.minutosTrabalhados() != null ? c.minutosTrabalhados() : 0L)
                .sum();

        return new StatusIntegracaoPontoDto(
                mes,
                ano,
                conferencia.isPresent(),
                conferencia.map(ConferenciaPontoMensal::getConferidoEm).orElse(null),
                conferencia.map(ConferenciaPontoMensal::getConferidoPorCpf).orElse(null),
                conferencia.map(c -> c.getIntegradoFinanceiroEm() != null).orElse(false),
                conferencia.map(ConferenciaPontoMensal::getIntegradoFinanceiroEm).orElse(null),
                comRegistro,
                totalMinutos);
    }

    @Transactional(readOnly = true)
    public boolean isMesConferido(Long instituicaoId, Integer mes, Integer ano) {
        return conferenciaRepository
                .findByInstituicao_IdAndMesCompetenciaAndAnoCompetencia(instituicaoId, mes, ano)
                .isPresent();
    }

    @Transactional(readOnly = true)
    public ResumoPontoColaboradorFolha resumoColaboradorFolha(
            Long instituicaoId, String cpfColaborador, Integer mes, Integer ano) {
        validarCompetencia(mes, ano);
        YearMonth competencia = YearMonth.of(ano, mes);
        List<RegistroDiaPonto> registros = registroRepository
                .findByInstituicao_IdAndCpfColaboradorAndDataRegistroBetweenOrderByDataRegistroAsc(
                        instituicaoId,
                        cpfColaborador,
                        competencia.atDay(1),
                        competencia.atEndOfMonth());

        long minutos = 0;
        int dias = 0;
        for (RegistroDiaPonto r : registros) {
            long m = FolhaPontoUtil.minutosTrabalhados(r.getHoraEntrada(), r.getHoraSaida());
            if (m > 0) {
                minutos += m;
                dias++;
            }
        }
        return new ResumoPontoColaboradorFolha(dias, minutos, FolhaPontoUtil.formatarHoras(minutos));
    }

    public record ResumoPontoColaboradorFolha(int diasTrabalhados, long minutosTrabalhados, String horasFormatadas) {
    }

    private ResumoPontoMensalDto montarResumo(Integer mes, Integer ano, List<RegistroDiaPonto> registros) {
        List<RegistroDiaPontoDto> dtos = registros.stream().map(RegistroDiaPontoDto::of).toList();
        long totalMinutos = registros.stream()
                .mapToLong(r -> FolhaPontoUtil.minutosTrabalhados(r.getHoraEntrada(), r.getHoraSaida()))
                .sum();
        int diasCompletos = (int) registros.stream()
                .filter(r -> r.getHoraEntrada() != null && r.getHoraSaida() != null)
                .count();
        return new ResumoPontoMensalDto(
                mes,
                ano,
                dtos,
                totalMinutos,
                FolhaPontoUtil.formatarHoras(totalMinutos),
                diasCompletos);
    }

    private void validarCompetencia(Integer mes, Integer ano) {
        ExcecaoDeDominio.quandoNulo(mes, "Informe o mês de competência.");
        ExcecaoDeDominio.quandoNulo(ano, "Informe o ano de competência.");
        ExcecaoDeDominio.quando(mes < 1 || mes > 12, "Mês de competência inválido.");
    }

    /** Só permite conferir meses já encerrados — evita bloquear marcações do mês em andamento. */
    private void validarCompetenciaEncerrada(Integer mes, Integer ano) {
        YearMonth competencia = YearMonth.of(ano, mes);
        YearMonth mesAtual = YearMonth.from(RelogioAplicacao.hoje());
        ExcecaoDeDominio.quando(!competencia.isBefore(mesAtual),
                "Só é possível conferir competências de meses já encerrados.");
    }

    private void validarCpf(String cpf) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF do colaborador inválido.");
    }

    /**
     * Libera marcações no mês atual somente após o RH conferir o mês anterior.
     * Na primeira competência da instituição, não exige conferência prévia.
     */
    private void validarLiberacaoMarcacao(Long instituicaoId, LocalDate data) {
        if (!conferenciaRepository.existsByInstituicao_Id(instituicaoId)) {
            return;
        }
        YearMonth competenciaAtual = YearMonth.from(data);
        YearMonth anterior = competenciaAtual.minusMonths(1);
        if (!isMesConferido(instituicaoId, anterior.getMonthValue(), anterior.getYear())) {
            throw new ExcecaoDeDominio(
                    "O RH ainda não conferiu a folha de ponto de "
                            + String.format("%02d", anterior.getMonthValue()) + "/" + anterior.getYear()
                            + ". Aguarde a conferência para registrar ponto neste mês.");
        }
    }

    private static final class AgregadoPonto {
        int diasCompletos;
        long minutos;
        boolean aberto;

        void acumular(RegistroDiaPonto registro) {
            if (registro.getHoraEntrada() != null && registro.getHoraSaida() == null) {
                aberto = true;
            }
            long m = FolhaPontoUtil.minutosTrabalhados(registro.getHoraEntrada(), registro.getHoraSaida());
            if (m > 0) {
                minutos += m;
                diasCompletos++;
            }
        }
    }

    @Transactional
    public SolicitacaoAjustePontoDto solicitarAjuste(
            Long instituicaoId,
            String cpfColaborador,
            String nomeColaborador,
            SolicitarAjustePontoFormDto form) {
        validarCpf(cpfColaborador);
        ExcecaoDeDominio.quandoNulo(form, "Dados obrigatórios.");
        ExcecaoDeDominio.quandoNulo(form.dataRegistro(), "Informe a data do registro.");
        ExcecaoDeDominio.quandoNuloOuVazio(form.justificativa(), "Informe a justificativa do ajuste.");
        ExcecaoDeDominio.quando(
                form.horaEntradaProposta() == null && form.horaSaidaProposta() == null,
                "Informe pelo menos um horário a ajustar.");

        if (isMesConferido(instituicaoId, form.dataRegistro().getMonthValue(), form.dataRegistro().getYear())) {
            throw new ExcecaoDeDominio("Competência já conferida pelo RH. Ajustes não são permitidos.");
        }

        RegistroDiaPonto registro = registroRepository
                .findByInstituicao_IdAndCpfColaboradorAndDataRegistro(instituicaoId, cpfColaborador, form.dataRegistro())
                .orElse(null);

        LocalDateTime entradaProposta = form.horaEntradaProposta() != null
                ? LocalDateTime.of(form.dataRegistro(), form.horaEntradaProposta()) : null;
        LocalDateTime saidaProposta = form.horaSaidaProposta() != null
                ? LocalDateTime.of(form.dataRegistro(), form.horaSaidaProposta()) : null;

        if (entradaProposta != null && saidaProposta != null && saidaProposta.isBefore(entradaProposta)) {
            throw new ExcecaoDeDominio("Horário de saída não pode ser anterior à entrada.");
        }

        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));

        SolicitacaoAjustePonto solicitacao = SolicitacaoAjustePonto.builder()
                .instituicao(instituicao)
                .cpfColaborador(cpfColaborador)
                .nomeColaborador(nomeColaborador)
                .dataRegistro(form.dataRegistro())
                .horaEntradaAtual(registro != null ? registro.getHoraEntrada() : null)
                .horaSaidaAtual(registro != null ? registro.getHoraSaida() : null)
                .horaEntradaProposta(entradaProposta)
                .horaSaidaProposta(saidaProposta)
                .justificativa(form.justificativa().trim())
                .status(StatusSolicitacaoAjustePonto.PENDENTE)
                .criadoEm(RelogioAplicacao.agora())
                .build();

        return SolicitacaoAjustePontoDto.of(ajustePontoRepository.save(solicitacao));
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAjustePontoDto> listarMeusAjustes(Long instituicaoId, String cpfColaborador) {
        validarCpf(cpfColaborador);
        return ajustePontoRepository
                .findByInstituicao_IdAndCpfColaboradorOrderByCriadoEmDesc(instituicaoId, cpfColaborador)
                .stream()
                .map(SolicitacaoAjustePontoDto::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAjustePontoDto> listarAjustesRh(Long instituicaoId, StatusSolicitacaoAjustePonto status) {
        if (status != null) {
            return ajustePontoRepository.findByInstituicao_IdAndStatusOrderByCriadoEmAsc(instituicaoId, status)
                    .stream()
                    .map(SolicitacaoAjustePontoDto::of)
                    .toList();
        }
        return ajustePontoRepository.findByInstituicao_IdOrderByCriadoEmDesc(instituicaoId)
                .stream()
                .map(SolicitacaoAjustePontoDto::of)
                .toList();
    }

    @Transactional
    public SolicitacaoAjustePontoDto decidirAjuste(
            Long instituicaoId,
            Long id,
            String cpfGestor,
            DecidirAjustePontoFormDto form) {
        ExcecaoDeDominio.quandoNulo(form, "Dados obrigatórios.");
        ExcecaoDeDominio.quando(
                form.status() != StatusSolicitacaoAjustePonto.APROVADO
                        && form.status() != StatusSolicitacaoAjustePonto.REJEITADO,
                "Status de decisão inválido.");

        SolicitacaoAjustePonto solicitacao = ajustePontoRepository.findById(id)
                .orElseThrow(() -> new ExcecaoDeDominio("Solicitação não encontrada."));
        ExcecaoDeDominio.quando(
                solicitacao.getInstituicao() == null || !instituicaoId.equals(solicitacao.getInstituicao().getId()),
                "Solicitação não pertence a esta instituição.");
        ExcecaoDeDominio.quando(
                solicitacao.getStatus() != StatusSolicitacaoAjustePonto.PENDENTE,
                "Solicitação já foi decidida.");

        solicitacao.setStatus(form.status());
        solicitacao.setDecididoEm(RelogioAplicacao.agora());
        solicitacao.setDecididoPorCpf(cpfGestor);
        solicitacao.setObservacaoGestor(form.observacaoGestor());

        if (form.status() == StatusSolicitacaoAjustePonto.APROVADO) {
            aplicarAjusteAprovado(instituicaoId, solicitacao);
        }

        return SolicitacaoAjustePontoDto.of(ajustePontoRepository.save(solicitacao));
    }

    private void aplicarAjusteAprovado(Long instituicaoId, SolicitacaoAjustePonto solicitacao) {
        RegistroDiaPonto registro = registroRepository
                .findByInstituicao_IdAndCpfColaboradorAndDataRegistro(
                        instituicaoId, solicitacao.getCpfColaborador(), solicitacao.getDataRegistro())
                .orElseGet(() -> RegistroDiaPonto.builder()
                        .instituicao(solicitacao.getInstituicao())
                        .cpfColaborador(solicitacao.getCpfColaborador())
                        .nomeColaborador(solicitacao.getNomeColaborador())
                        .dataRegistro(solicitacao.getDataRegistro())
                        .build());

        if (solicitacao.getHoraEntradaProposta() != null) {
            registro.setHoraEntrada(solicitacao.getHoraEntradaProposta());
        }
        if (solicitacao.getHoraSaidaProposta() != null) {
            registro.setHoraSaida(solicitacao.getHoraSaidaProposta());
        }
        registro.setNomeColaborador(solicitacao.getNomeColaborador());
        registroRepository.save(registro);
    }
}
