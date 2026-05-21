package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.DashboardFinanceiroDto;
import gerenciamentoDeAcademia.dto.MensalidadeResumoDto;
import gerenciamentoDeAcademia.servicos.financeiro.ServicoFinanceiro;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/dashboard/resumo")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'financeiro:visualizar')")
    public DashboardFinanceiroDto dashboard() {
        return servicoFinanceiro.obterDashboard();
    }

    @GetMapping("/mensalidades")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'financeiro:visualizar')")
    public List<MensalidadeResumoDto> mensalidades() {
        return servicoFinanceiro.listarMensalidades();
    }

    @GetMapping("/inadimplentes")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'financeiro:relatorio')")
    public List<MensalidadeResumoDto> inadimplentes() {
        return servicoFinanceiro.listarInadimplentes();
    }

    @PostMapping("/mensalidades/{cpf}/baixa")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'financeiro:visualizar')")
    public Map<String, String> baixaManual(@PathVariable String cpf) {
        servicoFinanceiro.registrarBaixaManual(cpf);
        return Map.of("message", "Baixa de mensalidade registrada para o mês atual.");
    }
}
