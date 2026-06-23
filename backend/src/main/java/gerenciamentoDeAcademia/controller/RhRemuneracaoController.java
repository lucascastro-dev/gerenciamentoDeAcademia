package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.DocumentoRemuneracaoDto;
import gerenciamentoDeAcademia.dto.PublicarHoleriteDto;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.servicos.colaborador.ServicoDocumentoRemuneracaoColaborador;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("rh/remuneracao")
@RequiredArgsConstructor
public class RhRemuneracaoController {

    private final ServicoDocumentoRemuneracaoColaborador servico;

    @PostMapping("/holerite/publicar")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'rh:holerite-lancamento')")
    public DocumentoRemuneracaoDto publicarHolerite(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody PublicarHoleriteDto dto) {
        return servico.publicarHolerite(
                instituicaoDaSessao(usuario),
                cpfPublicador(usuario),
                dto);
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
