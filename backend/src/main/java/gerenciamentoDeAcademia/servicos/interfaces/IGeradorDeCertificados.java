package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.DadosCertificadoDto;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface IGeradorDeCertificados {
    void gerarCertificado(MultipartFile background, DadosCertificadoDto dadosCertificado);

    void processarImagemDeFundo(MultipartFile imagemFundo);

    void salvarCertificadoEmJpg(BufferedImage imagemCertificado, String caminhoPasta, String nomeAluno) throws IOException;
}
