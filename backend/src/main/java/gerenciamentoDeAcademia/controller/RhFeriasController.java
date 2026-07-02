package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.DecidirSolicitacaoFeriasRequest;
import gerenciamentoDeAcademia.dto.SolicitacaoFeriasDto;
import gerenciamentoDeAcademia.enums.StatusSolicitacaoFerias;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.servicos.colaborador.ServicoFerias;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("rh/ferias")
@RequiredArgsConstructor
public class RhFeriasController {

    private final ServicoFerias servico;

    @GetMapping
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'rh:ferias')")
    public List<SolicitacaoFeriasDto> listar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(required = false) StatusSolicitacaoFerias status) {
        return servico.listarRh(instituicaoDaSessao(usuario), status);
    }

    @PostMapping("/{id}/decidir")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'rh:ferias-gerenciar')")
    public SolicitacaoFeriasDto decidir(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id,
            @Valid @RequestBody DecidirSolicitacaoFeriasRequest request) {
        return servico.decidirRh(
                instituicaoDaSessao(usuario),
                cpfDaSessao(usuario),
                id,
                request);
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
