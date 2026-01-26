package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.LoginRequisicaoDto;
import gerenciamentoDeAcademia.dto.LoginRetornoDto;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.infra.seguranca.TokenService;
import gerenciamentoDeAcademia.servicos.login.GerenciadorDeLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("login")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private GerenciadorDeLogin gerenciadorDeLogin;

    @PostMapping
    public ResponseEntity login(@RequestBody LoginRequisicaoDto data) {
        try {
            gerenciadorDeLogin.validarLogin(data.login(), data.vinculo());

            var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
            var auth = this.authenticationManager.authenticate(usernamePassword);

            var token = tokenService.gerarToken((Usuario) auth.getPrincipal());

            return ResponseEntity.ok(new LoginRetornoDto(token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }
}