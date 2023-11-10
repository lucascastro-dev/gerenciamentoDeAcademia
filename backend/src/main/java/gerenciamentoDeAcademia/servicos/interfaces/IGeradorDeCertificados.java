package gerenciamentoDeAcademia.servicos.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface IGeradorDeCertificados {
    void processarArquivoExcel(MultipartFile arquivo);
    public void processarImagemDeFundo(MultipartFile imagemFundo);
    BufferedImage gerarCertificado(String nome, String faixa);
    void salvarCertificadoEmJpg(BufferedImage imagemCertificado, String nome) throws IOException;
}
