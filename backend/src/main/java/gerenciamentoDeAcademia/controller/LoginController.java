package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.LoginRequisicaoDto;
import gerenciamentoDeAcademia.dto.LoginRetornoDto;
import gerenciamentoDeAcademia.dto.VinculoInstituicaoDto;
import gerenciamentoDeAcademia.excecao.ApplicationException;
import gerenciamentoDeAcademia.infra.seguranca.TokenService;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.servicos.login.GerenciadorDeLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("login")
public class LoginController {

    private static final String MSG_CREDENCIAIS_INVALIDAS = "Usuário ou senha inválidos.";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private GerenciadorDeLogin gerenciadorDeLogin;

    @GetMapping("/vinculos/{cpf}")
    public List<VinculoInstituicaoDto> listarVinculos(@PathVariable String cpf) {
        return gerenciadorDeLogin.listarInstituicoesPorCpf(cpf.replaceAll("\\D", ""));
    }

    @PostMapping("/solicitarRecuperacaoSenha")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> solicitarRecuperacaoSenha(@RequestBody Map<String, String> body) {
        String cpf = body != null && body.get("cpf") != null ? body.get("cpf").replaceAll("\\D", "") : "";
        String mensagem = gerenciadorDeLogin.solicitarRecuperacaoSenha(cpf);
        return Map.of("message", mensagem);
    }

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequisicaoDto data) {
        try {
            gerenciadorDeLogin.validarLogin(data.login(), data.vinculo());

            var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
            var auth = authenticationManager.authenticate(usernamePassword);
            var autenticado = (UsuarioAutenticado) auth.getPrincipal();
            var token = tokenService.gerarToken(autenticado);
            var tipoAcesso = gerenciadorDeLogin.resolverTipoAcesso(autenticado);
            var funcionario = autenticado.getFuncionario();
            var aluno = autenticado.getAluno();
            boolean planoAtivo = gerenciadorDeLogin.planoAtivoParaVinculo(data.vinculo(), tipoAcesso);

            var retorno = new LoginRetornoDto(
                    token,
                    funcionario != null ? funcionario.getNome() : (aluno != null ? aluno.getNome() : data.login()),
                    funcionario != null ? funcionario.getTipoFuncionario() : null,
                    funcionario != null && funcionario.isUsuarioMaster(),
                    gerenciadorDeLogin.obterPermissoes(autenticado),
                    tipoAcesso,
                    planoAtivo
            );

            return ResponseEntity.ok(retorno);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", MSG_CREDENCIAIS_INVALIDAS));
        } catch (ApplicationException e) {
            return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
        }
    }
}
