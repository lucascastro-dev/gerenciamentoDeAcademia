package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.ConfirmarPagamentoFolhaDto;
import gerenciamentoDeAcademia.dto.DocumentoRemuneracaoDto;
import gerenciamentoDeAcademia.dto.FolhaPagamentoColaboradorDto;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.servicos.colaborador.ServicoDocumentoRemuneracaoColaborador;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("financeiro/folha-pagamento")
@RequiredArgsConstructor
public class FolhaPagamentoController {

    private final ServicoDocumentoRemuneracaoColaborador servico;

    @GetMapping("/colaboradores")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'financeiro:cobranca')")
    public List<FolhaPagamentoColaboradorDto> listarColaboradores(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        return servico.listarColaboradoresFolha(instituicaoDaSessao(usuario), mes, ano);
    }

    @PostMapping("/confirmar-pagamento")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'financeiro:cobranca')")
    public Map<String, Object> confirmarPagamento(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody ConfirmarPagamentoFolhaDto dto) {
        DocumentoRemuneracaoDto recibo = servico.confirmarPagamento(
                instituicaoDaSessao(usuario),
                cpfPublicador(usuario),
                dto);
        return Map.of(
                "message", "Pagamento confirmado. Recibo publicado para o colaborador.",
                "recibo", recibo);
    }

    private Long instituicaoDaSessao(UsuarioAutenticado usuario) {
        Long instituicaoId = usuario != null ? usuario.getInstituicaoId() : null;
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição não identificada na sessão.");
        return instituicaoId;
    }

    private String cpfPublicador(UsuarioAutenticado usuario) {
        if (usuario == null || usuario.getFuncionario() == null) {
            return null;
        }
        return usuario.getFuncionario().getCpf();
    }
}
