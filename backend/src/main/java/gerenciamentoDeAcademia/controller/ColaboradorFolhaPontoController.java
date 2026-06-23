package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.ResumoPontoMensalDto;
import gerenciamentoDeAcademia.dto.StatusPontoHojeDto;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.servicos.colaborador.ServicoFolhaPonto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("colaborador/folha-ponto")
@RequiredArgsConstructor
public class ColaboradorFolhaPontoController {

    private final ServicoFolhaPonto servico;

    @GetMapping("/status-hoje")
    public StatusPontoHojeDto statusHoje(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servico.statusHoje(instituicaoDaSessao(usuario), cpfDaSessao(usuario));
    }

    @PostMapping("/marcar")
    @ResponseStatus(HttpStatus.CREATED)
    public StatusPontoHojeDto marcar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servico.marcarPonto(
                instituicaoDaSessao(usuario),
                cpfDaSessao(usuario),
                nomeDaSessao(usuario));
    }

    @GetMapping("/meu-mes")
    public ResumoPontoMensalDto meuMes(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        return servico.meuResumoMensal(instituicaoDaSessao(usuario), cpfDaSessao(usuario), mes, ano);
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
