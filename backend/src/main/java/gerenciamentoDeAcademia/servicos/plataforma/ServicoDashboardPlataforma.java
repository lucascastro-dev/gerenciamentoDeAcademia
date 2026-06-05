package gerenciamentoDeAcademia.servicos.plataforma;

import gerenciamentoDeAcademia.dto.DashboardFinanceiroPlataformaDto;
import gerenciamentoDeAcademia.dto.DashboardPlataformaDto;
import gerenciamentoDeAcademia.dto.InstituicaoResumoFinanceiroDto;
import gerenciamentoDeAcademia.entidades.AssinaturaPlataforma;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.repositorios.AssinaturaPlataformaRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoDashboardPlataforma {

    private final InstituicaoRepository instituicaoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final TurmaRepository turmaRepository;
    private final AssinaturaPlataformaRepository assinaturaRepository;

    public DashboardPlataformaDto resumoAdministrativo() {
        long ativas = instituicaoRepository.countByCadastroAtivoTrue();
        long inativas = instituicaoRepository.countCadastroInativo();
        long funcionarios = funcionarioRepository.count();
        long funcionariosAtivos = funcionarioRepository.findAll().stream()
                .filter(f -> Boolean.TRUE.equals(f.getCadastroAtivo()))
                .count();
        long pendentes = Math.max(0, funcionarios - funcionariosAtivos);
        long planosVencidos = instituicaoRepository.findAll().stream().filter(this::planoNaoVigente).count();

        return new DashboardPlataformaDto(
                ativas + inativas,
                ativas,
                inativas,
                funcionariosAtivos,
                pendentes,
                turmaRepository.count(),
                planosVencidos
        );
    }

    public DashboardFinanceiroPlataformaDto resumoFinanceiro() {
        List<Instituicao> instituicoes = instituicaoRepository.findAll();
        long pendentes = instituicaoRepository.countByStatusFinanceiro(StatusFinanceiroInstituicao.PENDENTE_PAGAMENTO);
        long vencidos = instituicoes.stream().filter(this::planoNaoVigente).count();
        long vigentes = instituicoes.stream()
                .filter(i -> assinaturaRepository.findByInstituicao_Id(i.getId())
                        .map(AssinaturaPlataforma::isVigente)
                        .orElse(false))
                .count();

        BigDecimal confirmada = BigDecimal.ZERO;
        BigDecimal aguardando = BigDecimal.ZERO;
        for (Instituicao i : instituicoes) {
            if (!Boolean.TRUE.equals(i.getCadastroAtivo())) {
                continue;
            }
            BigDecimal valor = valorEstimadoPlano(i.getId());
            if (i.getStatusFinanceiro() == StatusFinanceiroInstituicao.PENDENTE_PAGAMENTO) {
                aguardando = aguardando.add(valor);
            } else if (i.getStatusFinanceiro() == StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO) {
                confirmada = confirmada.add(valor);
            }
        }

        List<InstituicaoResumoFinanceiroDto> destaquesPendentes = instituicoes.stream()
                .filter(i -> i.getStatusFinanceiro() == StatusFinanceiroInstituicao.PENDENTE_PAGAMENTO)
                .sorted(Comparator.comparing(Instituicao::getRazaoSocial, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(this::mapearResumoFinanceiro)
                .toList();

        List<InstituicaoResumoFinanceiroDto> destaquesPlanoExpirado = instituicoes.stream()
                .filter(this::planoNaoVigente)
                .sorted(Comparator.comparing(Instituicao::getRazaoSocial, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(this::mapearResumoFinanceiro)
                .toList();

        return new DashboardFinanceiroPlataformaDto(
                pendentes,
                vencidos,
                vigentes,
                confirmada,
                aguardando,
                destaquesPendentes,
                destaquesPlanoExpirado
        );
    }

    private boolean planoNaoVigente(Instituicao instituicao) {
        return assinaturaRepository.findByInstituicao_Id(instituicao.getId())
                .map(a -> !a.isVigente())
                .orElse(true);
    }

    private InstituicaoResumoFinanceiroDto mapearResumoFinanceiro(Instituicao i) {
        var assinatura = assinaturaRepository.findByInstituicao_Id(i.getId()).orElse(null);
        PlanoInstituicaoTipo plano = assinatura != null ? assinatura.getPlano() : null;
        boolean vigente = assinatura != null && assinatura.isVigente();
        return new InstituicaoResumoFinanceiroDto(
                i.getId(),
                i.getRazaoSocial(),
                i.getCnpj(),
                i.getStatusFinanceiro(),
                plano,
                vigente
        );
    }

    private BigDecimal valorEstimadoPlano(Long instituicaoId) {
        return assinaturaRepository.findByInstituicao_Id(instituicaoId)
                .map(a -> estimarValor(a.getPlano()))
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal estimarValor(PlanoInstituicaoTipo plano) {
        if (plano == null) {
            return BigDecimal.ZERO;
        }
        return switch (plano) {
            case TRIAL_7_DIAS -> BigDecimal.ZERO;
            case MENSAL -> new BigDecimal("199.00");
            case TRIMESTRAL -> new BigDecimal("537.00");
            case SEMESTRAL -> new BigDecimal("999.00");
            case ANUAL -> new BigDecimal("1899.00");
        };
    }
}
