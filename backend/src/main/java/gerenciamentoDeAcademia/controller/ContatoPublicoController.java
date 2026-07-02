package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.ContatoPublicoRequest;
import gerenciamentoDeAcademia.servicos.integracoes.ServicoContatoPublico;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("publico/contato")
@RequiredArgsConstructor
public class ContatoPublicoController {

    private final ServicoContatoPublico servico;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> enviar(@Valid @RequestBody ContatoPublicoRequest request) {
        servico.enviar(request);
        return Map.of(
                "message",
                "Mensagem recebida com sucesso. Nossa equipe comercial retornará em até 1 dia útil.");
    }
}
