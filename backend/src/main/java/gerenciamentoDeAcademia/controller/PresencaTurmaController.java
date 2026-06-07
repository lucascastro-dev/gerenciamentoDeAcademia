package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.PresencaGradeDto;
import gerenciamentoDeAcademia.dto.PresencaSalvarDto;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.servicos.turma.ServicoPresencaTurma;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
@RequestMapping("turma/professor")
public class PresencaTurmaController {

    private final ServicoPresencaTurma servicoPresencaTurma;

    public PresencaTurmaController(ServicoPresencaTurma servicoPresencaTurma) {
        this.servicoPresencaTurma = servicoPresencaTurma;
    }

    @GetMapping("/{id}/presenca")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:presenca')")
    public PresencaGradeDto consultar(
            @PathVariable Long id,
            @RequestParam int ano,
            @RequestParam int mes,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicoPresencaTurma.montarGrade(id, ano, mes, usuario);
    }

    @PutMapping("/{id}/presenca")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:presenca')")
    public void salvar(
            @PathVariable Long id,
            @RequestBody PresencaSalvarDto dto,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        servicoPresencaTurma.salvar(id, dto, usuario);
    }

    @GetMapping("/{id}/presenca/pdf")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:presenca')")
    public ResponseEntity<byte[]> pdf(
            @PathVariable Long id,
            @RequestParam int ano,
            @RequestParam int mes,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        byte[] pdf = servicoPresencaTurma.gerarPdf(id, ano, mes, usuario);
        String nome = "presenca_turma_" + id + "_" + ano + "_" + mes + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
