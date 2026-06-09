package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AuditoriaRegistroDto;
import gerenciamentoDeAcademia.servicos.auditoria.ServicoConsultaAuditoria;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("auditoria")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AuditoriaController {

    private final ServicoConsultaAuditoria servicoConsultaAuditoria;

    @GetMapping("/lista")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'auditoria:consultar')")
    public List<AuditoriaRegistroDto> listar() {
        return servicoConsultaAuditoria.listarRegistros();
    }
}
