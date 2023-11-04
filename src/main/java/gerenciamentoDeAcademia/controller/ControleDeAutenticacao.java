package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AutenticacaoDto;
import gerenciamentoDeAcademia.dto.RegistroDto;
import gerenciamentoDeAcademia.servicos.ServicoDeAutenticacao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("auth")
public class ControleDeAutenticacao {
    @Autowired
    private ServicoDeAutenticacao servicoDeAutenticacao;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AutenticacaoDto data) {
        return servicoDeAutenticacao.realizarLogin(data);
    }

    @PostMapping("/cadastrar")
    public ResponseEntity cadastrar(@RequestBody @Valid RegistroDto data) {
        return servicoDeAutenticacao.cadastrarUsuario(data);
    }
}
