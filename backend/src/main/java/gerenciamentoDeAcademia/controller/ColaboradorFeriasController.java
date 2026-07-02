package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.CriarSolicitacaoFeriasRequest;
import gerenciamentoDeAcademia.dto.DecidirSolicitacaoFeriasRequest;
import gerenciamentoDeAcademia.dto.ResumoFeriasColaboradorDto;
import gerenciamentoDeAcademia.dto.SolicitacaoFeriasDto;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.servicos.colaborador.ServicoFerias;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("colaborador/ferias")
@RequiredArgsConstructor
public class ColaboradorFeriasController {

    private final ServicoFerias servico;

    @GetMapping("/resumo")
    public ResumoFeriasColaboradorDto resumo(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servico.resumoColaborador(instituicaoDaSessao(usuario), cpfDaSessao(usuario));
    }

    @PostMapping("/solicitar")
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitacaoFeriasDto solicitar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody CriarSolicitacaoFeriasRequest request) {
        return servico.solicitar(
                instituicaoDaSessao(usuario),
                cpfDaSessao(usuario),
                nomeDaSessao(usuario),
                request);
    }

    @PostMapping("/{id}/cancelar")
    public SolicitacaoFeriasDto cancelar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id) {
        return servico.cancelar(instituicaoDaSessao(usuario), cpfDaSessao(usuario), id);
    }

    private Long instituicaoDaSessao(UsuarioAutenticado usuario) {
        Long instituicaoId = usuario != null ? usuario.getInstituicaoId() : null;
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição não identificada na sessão.");
        return instituicaoId;
    }

    private String cpfDaSessao(UsuarioAutenticado usuario) {
        ExcecaoDeDominio.quando(usuario == null || usuario.getFuncionario() == null,
                "Sessão de colaborador inválida.");
        return usuario.getFuncionario().getCpf();
    }

    private String nomeDaSessao(UsuarioAutenticado usuario) {
        return usuario.getFuncionario().getNome();
    }
}
