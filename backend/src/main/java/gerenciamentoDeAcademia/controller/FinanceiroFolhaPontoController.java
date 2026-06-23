package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.StatusIntegracaoPontoDto;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.servicos.colaborador.ServicoFolhaPonto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("financeiro/folha-pagamento/ponto")
@RequiredArgsConstructor
public class FinanceiroFolhaPontoController {

    private final ServicoFolhaPonto servico;

    @GetMapping("/status-integracao")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'financeiro:cobranca')")
    public StatusIntegracaoPontoDto statusIntegracao(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        return servico.statusIntegracao(instituicaoDaSessao(usuario), mes, ano);
    }

    @PostMapping("/integrar")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'financeiro:cobranca')")
    public Map<String, Object> integrar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        StatusIntegracaoPontoDto status = servico.integrarFinanceiro(
                instituicaoDaSessao(usuario),
                cpfDaSessao(usuario),
                mes,
                ano);
        return Map.of(
                "message", "Ponto integrado ao financeiro. Pagamentos podem ser confirmados após holerites publicados.",
                "status", status);
    }

    private Long instituicaoDaSessao(UsuarioAutenticado usuario) {
        Long instituicaoId = usuario != null ? usuario.getInstituicaoId() : null;
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição não identificada na sessão.");
        return instituicaoId;
    }

    private String cpfDaSessao(UsuarioAutenticado usuario) {
        if (usuario == null || usuario.getFuncionario() == null) {
            return null;
        }
        return usuario.getFuncionario().getCpf();
    }
}
