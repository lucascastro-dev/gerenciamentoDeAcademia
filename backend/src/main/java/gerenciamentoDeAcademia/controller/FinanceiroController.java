package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.DashboardFinanceiroDto;
import gerenciamentoDeAcademia.dto.DashboardFinanceiroPlataformaDto;
import gerenciamentoDeAcademia.servicos.plataforma.ServicoDashboardPlataforma;
import gerenciamentoDeAcademia.dto.MensalidadeResumoDto;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.servicos.financeiro.ServicoFinanceiro;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("financeiro")
@RequiredArgsConstructor
public class FinanceiroController {

    private final ServicoFinanceiro servicoFinanceiro;
    private final ServicoDashboardPlataforma servicoDashboardPlataforma;

    @GetMapping("/plataforma/resumo")
    @PreAuthorize("@permissaoEvaluator.possuiMaster(authentication)")
    public DashboardFinanceiroPlataformaDto dashboardPlataforma() {
        return servicoDashboardPlataforma.resumoFinanceiro();
    }

    @GetMapping("/dashboard/resumo")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'financeiro:visualizar')")
    public DashboardFinanceiroDto dashboard(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicoFinanceiro.obterDashboard(instituicaoDaSessao(usuario));
    }

    @GetMapping("/mensalidades")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'financeiro:visualizar')")
    public List<MensalidadeResumoDto> mensalidades(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicoFinanceiro.listarMensalidades(instituicaoDaSessao(usuario));
    }

    @GetMapping("/inadimplentes")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'financeiro:relatorio')")
    public List<MensalidadeResumoDto> inadimplentes(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicoFinanceiro.listarInadimplentes(instituicaoDaSessao(usuario));
    }

    @PostMapping("/mensalidades/{cpf}/baixa")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'financeiro:visualizar')")
    public Map<String, String> baixaManual(
            @PathVariable String cpf,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        servicoFinanceiro.registrarBaixaManual(cpf, instituicaoDaSessao(usuario));
        return Map.of("message", "Baixa de mensalidade registrada para o mês atual.");
    }

    private Long instituicaoDaSessao(UsuarioAutenticado usuario) {
        Long instituicaoId = usuario != null ? usuario.getInstituicaoId() : null;
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição não identificada na sessão.");
        return instituicaoId;
    }
}
