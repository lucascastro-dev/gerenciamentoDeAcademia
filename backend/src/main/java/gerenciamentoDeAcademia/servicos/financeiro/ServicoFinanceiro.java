package gerenciamentoDeAcademia.servicos.financeiro;

import gerenciamentoDeAcademia.dto.DashboardFinanceiroDto;
import gerenciamentoDeAcademia.dto.MensalidadeResumoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
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

    private final AlunoRepository alunoRepository;

    public DashboardFinanceiroDto obterDashboard() {
        List<Aluno> alunos = alunoRepository.findAll();
        LocalDate hoje = LocalDate.now();

        List<MensalidadeResumoDto> resumos = alunos.stream()
                .map(a -> toResumo(a, hoje))
                .sorted(Comparator.comparing(MensalidadeResumoDto::inadimplente).reversed()
                        .thenComparing(MensalidadeResumoDto::diaVencimento))
                .toList();

        long inadimplentes = resumos.stream().filter(MensalidadeResumoDto::inadimplente).count();
        double receitaPrevista = alunos.stream()
                .mapToDouble(a -> a.getValorMensalidade() != null ? a.getValorMensalidade() : 0)
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
                alunos.size(),
                receitaPrevista,
                inadimplentes,
                valorInadimplente,
                proximos
        );
    }

    public List<MensalidadeResumoDto> listarMensalidades() {
        LocalDate hoje = LocalDate.now();
        return alunoRepository.findAll().stream()
                .map(a -> toResumo(a, hoje))
                .toList();
    }

    public List<MensalidadeResumoDto> listarInadimplentes() {
        return listarMensalidades().stream().filter(MensalidadeResumoDto::inadimplente).toList();
    }

    public MensalidadeResumoDto resumoMensalidade(String cpf) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF do aluno é obrigatório");
        Aluno aluno = alunoRepository.findByCpf(cpf.replaceAll("\\D", ""));
        ExcecaoDeDominio.quandoNulo(aluno, "Aluno não encontrado");
        return toResumo(aluno, LocalDate.now());
    }

    @Transactional
    public void registrarBaixaManual(String cpf) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF do aluno é obrigatório");
        Aluno aluno = alunoRepository.findByCpf(cpf.replaceAll("\\D", ""));
        ExcecaoDeDominio.quandoNulo(aluno, "Aluno não encontrado");
        aluno.setDataUltimoPagamentoMensalidade(LocalDate.now());
        alunoRepository.save(aluno);
    }

    private MensalidadeResumoDto toResumo(Aluno aluno, LocalDate hoje) {
        return new MensalidadeResumoDto(
                aluno.getCpf(),
                aluno.getNome(),
                aluno.getValorMensalidade(),
                aluno.getDiaVencimentoMensalidade(),
                isInadimplente(aluno, hoje),
                aluno.getDataUltimoPagamentoMensalidade()
        );
    }

    public SituacaoCobranca situacaoMensalidade(Aluno aluno, LocalDate hoje, int diasTolerancia) {
        if (pagouNoMesAtual(aluno, hoje)) {
            return SituacaoCobranca.ATIVO;
        }
        if (aluno.getDiaVencimentoMensalidade() == null) {
            return SituacaoCobranca.ATIVO;
        }
        LocalDate vencimento = vencimentoNoMes(aluno, YearMonth.from(hoje));
        if (!hoje.isAfter(vencimento)) {
            return SituacaoCobranca.ATIVO;
        }
        long diasAtraso = ChronoUnit.DAYS.between(vencimento, hoje);
        if (diasAtraso <= diasTolerancia) {
            return SituacaoCobranca.EM_TOLERANCIA;
        }
        return SituacaoCobranca.BLOQUEADO;
    }

    private LocalDate vencimentoNoMes(Aluno aluno, YearMonth mes) {
        int dia = Math.min(aluno.getDiaVencimentoMensalidade(), mes.lengthOfMonth());
        return mes.atDay(dia);
    }

    private boolean isInadimplente(Aluno aluno, LocalDate hoje) {
        SituacaoCobranca situacao = situacaoMensalidade(aluno, hoje, 0);
        return situacao == SituacaoCobranca.EM_TOLERANCIA || situacao == SituacaoCobranca.BLOQUEADO;
    }

    private boolean pagouNoMesAtual(Aluno aluno, LocalDate hoje) {
        LocalDate pago = aluno.getDataUltimoPagamentoMensalidade();
        if (pago == null) {
            return false;
        }
        YearMonth ref = YearMonth.from(hoje);
        return YearMonth.from(pago).equals(ref);
    }
}
