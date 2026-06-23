package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.FolhaPontoColaboradorRhDto;
import gerenciamentoDeAcademia.dto.ResumoPontoMensalDto;
import gerenciamentoDeAcademia.dto.StatusIntegracaoPontoDto;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.servicos.colaborador.ServicoFolhaPonto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("rh/folha-ponto")
@RequiredArgsConstructor
public class RhFolhaPontoController {

    private final ServicoFolhaPonto servico;

    @GetMapping("/colaboradores")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'rh:folha-ponto')")
    public List<FolhaPontoColaboradorRhDto> listarColaboradores(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        return servico.listarColaboradoresRh(instituicaoDaSessao(usuario), mes, ano);
    }

    @GetMapping("/colaboradores/{cpf}/detalhe")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'rh:folha-ponto')")
    public ResumoPontoMensalDto detalheColaborador(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable String cpf,
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        return servico.detalheColaboradorRh(instituicaoDaSessao(usuario), cpf.replaceAll("\\D", ""), mes, ano);
    }

    @GetMapping("/status-integracao")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'rh:folha-ponto')")
    public StatusIntegracaoPontoDto statusIntegracao(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        return servico.statusIntegracao(instituicaoDaSessao(usuario), mes, ano);
    }

    @PostMapping("/conferir")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'rh:folha-ponto')")
    public Map<String, Object> conferir(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        StatusIntegracaoPontoDto status = servico.conferirMesRh(
                instituicaoDaSessao(usuario),
                cpfDaSessao(usuario),
                mes,
                ano);
        return Map.of(
                "message", "Folha de ponto conferida. Financeiro pode integrar na folha de pagamento.",
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
