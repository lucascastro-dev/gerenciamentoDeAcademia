package gerenciamentoDeAcademia.servicos.colaborador;

import gerenciamentoDeAcademia.dto.FolhaPontoColaboradorRhDto;
import gerenciamentoDeAcademia.dto.RegistroDiaPontoDto;
import gerenciamentoDeAcademia.dto.ResumoPontoMensalDto;
import gerenciamentoDeAcademia.dto.StatusIntegracaoPontoDto;
import gerenciamentoDeAcademia.dto.StatusPontoHojeDto;
import gerenciamentoDeAcademia.entidades.ConferenciaPontoMensal;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.RegistroDiaPonto;
import gerenciamentoDeAcademia.entidades.VinculoFuncionarioInstituicao;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.ConferenciaPontoMensalRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.RegistroDiaPontoRepository;
import gerenciamentoDeAcademia.repositorios.VinculoFuncionarioInstituicaoRepository;
import gerenciamentoDeAcademia.util.FolhaPontoUtil;
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

    @Transactional(readOnly = true)
    public StatusPontoHojeDto statusHoje(Long instituicaoId, String cpfColaborador) {
        validarCpf(cpfColaborador);
        LocalDate hoje = LocalDate.now();
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

        if (isMesConferido(instituicaoId, LocalDate.now().getMonthValue(), LocalDate.now().getYear())) {
            throw new ExcecaoDeDominio("O ponto deste mês já foi conferido pelo RH. Novas marcações estão bloqueadas.");
        }

        LocalDate hoje = LocalDate.now();
        LocalDateTime agora = LocalDateTime.now();

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

        conferencia.setConferidoEm(LocalDateTime.now());
        conferencia.setConferidoPorCpf(cpfConferidor);
        conferenciaRepository.save(conferencia);

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

        conferencia.setIntegradoFinanceiroEm(LocalDateTime.now());
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

    private void validarCpf(String cpf) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF do colaborador inválido.");
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
}
