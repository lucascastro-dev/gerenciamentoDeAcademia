package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.servicos.GeradorDeCertificados;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin("*")
public class GeradorDeCertificadoController {
    private final GeradorDeCertificados geradorDeCertificado;

    public GeradorDeCertificadoController(GeradorDeCertificados geradorDeCertificado) {
        this.geradorDeCertificado = geradorDeCertificado;
    }

    @PostMapping("/upload")
    public void handleFileUpload(@RequestParam("file") MultipartFile file,
                                 @RequestParam("background") MultipartFile background) {

        geradorDeCertificado.processarImagemDeFundo(background);
        geradorDeCertificado.processarArquivoExcel(file);
    }
}
