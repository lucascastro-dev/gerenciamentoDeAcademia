package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.integracoes.IntegracoesStatusDto;
import gerenciamentoDeAcademia.servicos.integracoes.ServicoNotificacoes;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("integracoes")
@RequiredArgsConstructor
public class IntegracoesStatusController {

    private final ServicoNotificacoes servicoNotificacoes;

    @GetMapping("/status")
    public Map<String, Object> status() {
        IntegracoesStatusDto dto = servicoNotificacoes.status();
        return dto.asMap();
    }
}
