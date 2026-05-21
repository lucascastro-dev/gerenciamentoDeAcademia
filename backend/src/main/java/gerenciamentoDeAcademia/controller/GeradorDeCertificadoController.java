package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.DadosCertificadoDto;
import gerenciamentoDeAcademia.servicos.GeradorDeCertificados;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/certificado")
@CrossOrigin("*")
public class GeradorDeCertificadoController {
    private final GeradorDeCertificados geradorDeCertificado;

    public GeradorDeCertificadoController(GeradorDeCertificados geradorDeCertificado) {
        this.geradorDeCertificado = geradorDeCertificado;
    }

    @PostMapping("/gerarCertificadoJudo")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'certificado:gerar')")
    public ResponseEntity<String> gerarCertificado(@RequestBody DadosCertificadoDto dadosCertificado) {
        geradorDeCertificado.gerarCertificado(dadosCertificado);
        return ResponseEntity.ok("Certificados gerados com sucesso.");
    }
}
