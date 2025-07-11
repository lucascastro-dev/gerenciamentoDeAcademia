package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.DadosCertificadoDto;
import gerenciamentoDeAcademia.servicos.GeradorDeCertificados;
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
    public void handleFileUpload(@RequestBody DadosCertificadoDto dadosCertificado) {
        geradorDeCertificado.gerarCertificado(dadosCertificado);
    }
}
