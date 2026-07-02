package gerenciamentoDeAcademia.servicos.financeiro;

import gerenciamentoDeAcademia.dto.DashboardFinanceiroDto;
import gerenciamentoDeAcademia.dto.MensalidadeHistoricoItemDto;
import gerenciamentoDeAcademia.dto.MensalidadeResumoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.CobrancaExterna;
import gerenciamentoDeAcademia.entidades.MatriculaInstituicao;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.StatusCobrancaExterna;
import gerenciamentoDeAcademia.enums.TipoCobrancaExterna;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.CobrancaExternaRepository;
import gerenciamentoDeAcademia.repositorios.MatriculaInstituicaoRepository;
import gerenciamentoDeAcademia.servicos.aluno.ServicoMatriculaInstituicao;
import gerenciamentoDeAcademia.util.RelogioAplicacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicoFinanceiro {

    private final MatriculaInstituicaoRepository matriculaInstituicaoRepository;
    private final ServicoMatriculaInstituicao servicoMatriculaInstituicao;
    private final CobrancaExternaRepository cobrancaExternaRepository;

    public DashboardFinanceiroDto obterDashboard(Long instituicaoId) {
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição é obrigatória para o dashboard financeiro.");
        List<MatriculaInstituicao> matriculas = matriculaInstituicaoRepository
                .findByInstituicao_IdOrderByAluno_NomeAsc(instituicaoId);

        List<MensalidadeResumoDto> resumos = listarMensalidades(instituicaoId).stream()
                .sorted(Comparator.comparing(MensalidadeResumoDto::inadimplente).reversed()
                        .thenComparing(MensalidadeResumoDto::diaVencimento))
                .toList();

        long inadimplentes = resumos.stream().filter(MensalidadeResumoDto::inadimplente).count();
        double receitaPrevista = matriculas.stream()
                .mapToDouble(m -> m.getValorMensalidade() != null ? m.getValorMensalidade() : 0)
                .sum();
        double valorInadimplente = resumos.stream()
                .filter(MensalidadeResumoDto::inadimplente)
                .mapToDouble(m -> m.valorMensalidade() != null ? m.valorMensalidade() : 0)
                .sum();

        List<MensalidadeResumoDto> proximos = resumos.stream()
                .filter(m -> !m.inadimplente())
                .limit(10)
                .toList();

        return new DashboardFinanceiroDto(
                matriculas.size(),
                receitaPrevista,
                inadimplentes,
                valorInadimplente,
                proximos
        );
    }

    public List<MensalidadeResumoDto> listarMensalidades(Long instituicaoId) {
        LocalDate hoje = RelogioAplicacao.hoje();
        return listarMensalidades(instituicaoId, hoje.getMonthValue(), hoje.getYear());
    }

    public List<MensalidadeResumoDto> listarMensalidades(Long instituicaoId, int mes, int ano) {
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição é obrigatória.");
        validarCompetencia(mes, ano);
        LocalDate hoje = RelogioAplicacao.hoje();
        YearMonth competencia = YearMonth.of(ano, mes);

        Map<String, CobrancaExterna> cobrancaPorCpf = cobrancaExternaRepository
                .findByInstituicao_IdAndTipoAndMesCompetenciaAndAnoCompetencia(
                        instituicaoId, TipoCobrancaExterna.MENSALIDADE_ALUNO, mes, ano)
                .stream()
                .collect(Collectors.toMap(CobrancaExterna::getCpfAluno, c -> c, (a, b) -> b));

        return matriculaInstituicaoRepository.findByInstituicao_IdOrderByAluno_NomeAsc(instituicaoId).stream()
                .map(m -> toResumoCompetencia(
                        m,
                        competencia,
                        hoje,
                        cobrancaPorCpf.get(m.getAluno().getCpf())))
                .toList();
    }

    public List<MensalidadeResumoDto> listarInadimplentes(Long instituicaoId) {
        LocalDate hoje = RelogioAplicacao.hoje();
        return listarInadimplentes(instituicaoId, hoje.getMonthValue(), hoje.getYear());
    }

    public List<MensalidadeResumoDto> listarInadimplentes(Long instituicaoId, int mes, int ano) {
        return listarMensalidades(instituicaoId, mes, ano).stream().filter(MensalidadeResumoDto::inadimplente).toList();
    }

    public MensalidadeResumoDto resumoMensalidade(String cpf, Long instituicaoId) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF do aluno é obrigatório");
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição é obrigatória.");
        String cpfLimpo = cpf.replaceAll("\\D", "");
        MatriculaInstituicao matricula = servicoMatriculaInstituicao.obterOuMigrarLegado(cpfLimpo, instituicaoId);
        ExcecaoDeDominio.quandoNulo(matricula, "Matrícula financeira não encontrada para esta instituição.");
        LocalDate hoje = RelogioAplicacao.hoje();
        CobrancaExterna cobranca = cobrancaExternaRepository
                .findByInstituicao_IdAndTipoAndMesCompetenciaAndAnoCompetencia(
                        instituicaoId,
                        TipoCobrancaExterna.MENSALIDADE_ALUNO,
                        hoje.getMonthValue(),
                        hoje.getYear())
                .stream()
                .filter(c -> cpfLimpo.equals(c.getCpfAluno()))
                .findFirst()
                .orElse(null);
        return toResumoCompetencia(matricula, YearMonth.from(hoje), hoje, cobranca);
    }

    public LocalDate vencimentoCompetencia(String cpf, Long instituicaoId, int mes, int ano) {
        MatriculaInstituicao matricula = servicoMatriculaInstituicao.obterOuMigrarLegado(
                cpf.replaceAll("\\D", ""), instituicaoId);
        ExcecaoDeDominio.quandoNulo(matricula, "Matrícula financeira não encontrada.");
        return vencimentoNoMes(matricula, YearMonth.of(ano, mes));
    }

    public List<MensalidadeHistoricoItemDto> listarHistoricoAnual(String cpf, Long instituicaoId, int ano) {
        MatriculaInstituicao matricula = servicoMatriculaInstituicao.obterOuMigrarLegado(
                cpf.replaceAll("\\D", ""), instituicaoId);
        ExcecaoDeDominio.quandoNulo(matricula, "Matrícula financeira não encontrada.");

        LocalDate hoje = RelogioAplicacao.hoje();
        BigDecimal valor = matricula.getValorMensalidade() != null
                ? BigDecimal.valueOf(matricula.getValorMensalidade()) : BigDecimal.ZERO;

        List<CobrancaExterna> cobrancas = cobrancaExternaRepository
                .findByInstituicao_IdAndCpfAlunoAndTipoAndAnoCompetenciaOrderByMesCompetenciaAsc(
                        instituicaoId, cpf.replaceAll("\\D", ""), TipoCobrancaExterna.MENSALIDADE_ALUNO, ano);

        Map<Integer, CobrancaExterna> cobrancaPorMes = cobrancas.stream()
                .filter(c -> c.getMesCompetencia() != null)
                .collect(Collectors.toMap(CobrancaExterna::getMesCompetencia, c -> c, (a, b) -> b));

        List<MensalidadeHistoricoItemDto> itens = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            YearMonth ym = YearMonth.of(ano, mes);
            CobrancaExterna cobranca = cobrancaPorMes.get(mes);
            CompetenciaMensalidade competencia = avaliarCompetencia(matricula, ym, hoje, cobranca);

            itens.add(new MensalidadeHistoricoItemDto(
                    mes, ano, competencia.vencimento(), competencia.dataPagamento(),
                    competencia.status(), competencia.statusDescricao(), valor,
                    competencia.cobrancaId(), competencia.podeGerarCobranca()));
        }
        return itens;
    }

    private record CompetenciaMensalidade(
            String status,
            String statusDescricao,
            LocalDate vencimento,
            LocalDate dataPagamento,
            Long cobrancaId,
            boolean podeGerarCobranca,
            boolean inadimplente
    ) {
    }

    private CompetenciaMensalidade avaliarCompetencia(
            MatriculaInstituicao matricula,
            YearMonth competencia,
            LocalDate hoje,
            CobrancaExterna cobranca) {
        LocalDate vencimento = vencimentoNoMes(matricula, competencia);
        String status;
        String statusDescricao;
        LocalDate dataPagamento = null;
        Long cobrancaId = null;
        boolean podeGerar = false;

        if (cobranca != null && cobranca.getStatus() == StatusCobrancaExterna.PAGO) {
            status = "PAGO";
            statusDescricao = "Pago";
            dataPagamento = cobranca.getPagoEm() != null ? cobranca.getPagoEm().toLocalDate() : null;
            cobrancaId = cobranca.getId();
        } else if (pagouNaCompetencia(matricula, competencia)) {
            status = "PAGO";
            statusDescricao = "Pago";
            dataPagamento = matricula.getDataUltimoPagamentoMensalidade();
        } else if (cobranca != null && cobranca.getStatus() == StatusCobrancaExterna.PENDENTE) {
            status = "PENDENTE_COBRANCA";
            statusDescricao = "Cobrança gerada";
            cobrancaId = cobranca.getId();
        } else if (competencia.isAfter(YearMonth.from(hoje))) {
            status = "A_VENCER";
            statusDescricao = "A vencer";
        } else if (hoje.isAfter(vencimento)) {
            status = "EM_ABERTO";
            statusDescricao = "Em aberto";
            podeGerar = true;
        } else {
            status = "A_VENCER";
            statusDescricao = "A vencer";
        }

        if (status.equals("EM_ABERTO") && cobranca == null) {
            podeGerar = true;
        }

        boolean inadimplente = status.equals("EM_ABERTO")
                || (status.equals("PENDENTE_COBRANCA") && !hoje.isBefore(vencimento));

        return new CompetenciaMensalidade(
                status, statusDescricao, vencimento, dataPagamento, cobrancaId, podeGerar, inadimplente);
    }

    private void validarCompetencia(int mes, int ano) {
        ExcecaoDeDominio.quando(mes < 1 || mes > 12, "Mês de competência inválido.");
        ExcecaoDeDominio.quando(ano < 2000 || ano > 2100, "Ano de competência inválido.");
    }

    private boolean pagouNaCompetencia(MatriculaInstituicao matricula, YearMonth competencia) {
        LocalDate pago = matricula.getDataUltimoPagamentoMensalidade();
        return pago != null && YearMonth.from(pago).equals(competencia);
    }

    @Transactional
    public void registrarBaixaManual(String cpf, Long instituicaoId) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF do aluno é obrigatório");
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição é obrigatória.");
        servicoMatriculaInstituicao.registrarBaixa(cpf.replaceAll("\\D", ""), instituicaoId);
    }

    public SituacaoCobranca situacaoMensalidade(MatriculaInstituicao matricula, LocalDate hoje, int diasTolerancia) {
        if (matricula == null) {
            return SituacaoCobranca.BLOQUEADO;
        }
        if (pagouNoMesAtual(matricula, hoje)) {
            return SituacaoCobranca.ATIVO;
        }
        if (matricula.getDiaVencimentoMensalidade() == null) {
            return SituacaoCobranca.ATIVO;
        }
        LocalDate vencimento = vencimentoNoMes(matricula, YearMonth.from(hoje));
        if (!hoje.isAfter(vencimento)) {
            return SituacaoCobranca.ATIVO;
        }
        long diasAtraso = ChronoUnit.DAYS.between(vencimento, hoje);
        if (diasAtraso <= diasTolerancia) {
            return SituacaoCobranca.EM_TOLERANCIA;
        }
        return SituacaoCobranca.BLOQUEADO;
    }

    /** @deprecated use {@link #situacaoMensalidade(MatriculaInstituicao, LocalDate, int)} */
    @Deprecated
    public SituacaoCobranca situacaoMensalidade(Aluno aluno, LocalDate hoje, int diasTolerancia) {
        if (aluno == null) {
            return SituacaoCobranca.BLOQUEADO;
        }
        MatriculaInstituicao legado = new MatriculaInstituicao();
        legado.setValorMensalidade(aluno.getValorMensalidade());
        legado.setDiaVencimentoMensalidade(aluno.getDiaVencimentoMensalidade());
        legado.setDataUltimoPagamentoMensalidade(aluno.getDataUltimoPagamentoMensalidade());
        return situacaoMensalidade(legado, hoje, diasTolerancia);
    }

    private MensalidadeResumoDto toResumoCompetencia(
            MatriculaInstituicao matricula,
            YearMonth competencia,
            LocalDate hoje,
            CobrancaExterna cobranca) {
        Aluno aluno = matricula.getAluno();
        CompetenciaMensalidade avaliacao = avaliarCompetencia(matricula, competencia, hoje, cobranca);
        return new MensalidadeResumoDto(
                aluno.getCpf(),
                aluno.getNome(),
                matricula.getValorMensalidade(),
                matricula.getDiaVencimentoMensalidade(),
                avaliacao.inadimplente(),
                avaliacao.dataPagamento() != null
                        ? avaliacao.dataPagamento()
                        : matricula.getDataUltimoPagamentoMensalidade(),
                avaliacao.statusDescricao(),
                avaliacao.vencimento()
        );
    }

    private LocalDate vencimentoNoMes(MatriculaInstituicao matricula, YearMonth mes) {
        int dia = Math.min(matricula.getDiaVencimentoMensalidade(), mes.lengthOfMonth());
        return mes.atDay(dia);
    }

    private boolean pagouNoMesAtual(MatriculaInstituicao matricula, LocalDate hoje) {
        LocalDate pago = matricula.getDataUltimoPagamentoMensalidade();
        if (pago == null) {
            return false;
        }
        YearMonth ref = YearMonth.from(hoje);
        return YearMonth.from(pago).equals(ref);
    }
}
