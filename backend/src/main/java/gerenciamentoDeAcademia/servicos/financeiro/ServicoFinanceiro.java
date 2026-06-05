package gerenciamentoDeAcademia.servicos.financeiro;

import gerenciamentoDeAcademia.dto.DashboardFinanceiroDto;
import gerenciamentoDeAcademia.dto.MensalidadeResumoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.MatriculaInstituicao;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.MatriculaInstituicaoRepository;
import gerenciamentoDeAcademia.servicos.aluno.ServicoMatriculaInstituicao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoFinanceiro {

    private final MatriculaInstituicaoRepository matriculaInstituicaoRepository;
    private final ServicoMatriculaInstituicao servicoMatriculaInstituicao;

    public DashboardFinanceiroDto obterDashboard(Long instituicaoId) {
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição é obrigatória para o dashboard financeiro.");
        List<MatriculaInstituicao> matriculas = matriculaInstituicaoRepository
                .findByInstituicao_IdOrderByAluno_NomeAsc(instituicaoId);
        LocalDate hoje = LocalDate.now();

        List<MensalidadeResumoDto> resumos = matriculas.stream()
                .map(m -> toResumo(m, hoje))
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
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição é obrigatória.");
        LocalDate hoje = LocalDate.now();
        return matriculaInstituicaoRepository.findByInstituicao_IdOrderByAluno_NomeAsc(instituicaoId).stream()
                .map(m -> toResumo(m, hoje))
                .toList();
    }

    public List<MensalidadeResumoDto> listarInadimplentes(Long instituicaoId) {
        return listarMensalidades(instituicaoId).stream().filter(MensalidadeResumoDto::inadimplente).toList();
    }

    public MensalidadeResumoDto resumoMensalidade(String cpf, Long instituicaoId) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF do aluno é obrigatório");
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição é obrigatória.");
        MatriculaInstituicao matricula = servicoMatriculaInstituicao.obterOuMigrarLegado(
                cpf.replaceAll("\\D", ""), instituicaoId);
        ExcecaoDeDominio.quandoNulo(matricula, "Matrícula financeira não encontrada para esta instituição.");
        return toResumo(matricula, LocalDate.now());
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

    private MensalidadeResumoDto toResumo(MatriculaInstituicao matricula, LocalDate hoje) {
        Aluno aluno = matricula.getAluno();
        return new MensalidadeResumoDto(
                aluno.getCpf(),
                aluno.getNome(),
                matricula.getValorMensalidade(),
                matricula.getDiaVencimentoMensalidade(),
                isInadimplente(matricula, hoje),
                matricula.getDataUltimoPagamentoMensalidade()
        );
    }

    private LocalDate vencimentoNoMes(MatriculaInstituicao matricula, YearMonth mes) {
        int dia = Math.min(matricula.getDiaVencimentoMensalidade(), mes.lengthOfMonth());
        return mes.atDay(dia);
    }

    private boolean isInadimplente(MatriculaInstituicao matricula, LocalDate hoje) {
        SituacaoCobranca situacao = situacaoMensalidade(matricula, hoje, 0);
        return situacao == SituacaoCobranca.EM_TOLERANCIA || situacao == SituacaoCobranca.BLOQUEADO;
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
