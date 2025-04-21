package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.DadosCertificadoDto;
import gerenciamentoDeAcademia.servicos.interfaces.IGeradorDeCertificados;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;

@Service
public class GeradorDeCertificados implements IGeradorDeCertificados {
    private static final int LARGURA_CERTIFICADO = 3508;
    private static final int ALTURA_CERTIFICADO = 2480;
    private static final String FONTE_PADRAO = "Brush Script MT";
    private static final String FONTE_DATA = "Arial";
    private static final int TAMANHO_FONTE_PADRAO = 150;
    private static final int TAMANHO_FONTE_DATA = 50;
    private static final String FORMATO_DATA = "dd/MM/yyyy";
    private static final String CAMINHO_BASE = "C:/certificado/";

    private String backgroundImagePath;

    @Override
    public void gerarCertificado(MultipartFile background, DadosCertificadoDto dadosCertificado) {
        if (background == null || dadosCertificado == null) {
            throw new IllegalArgumentException("Background ou dados do certificado não podem ser nulos.");
        }

        processarImagemDeFundo(background);

        String caminhoPastaProfessor = CAMINHO_BASE + dadosCertificado.getProfessor();
        criarDiretorioSeNecessario(caminhoPastaProfessor);

        dadosCertificado.getAlunos().forEach(aluno -> {
            BufferedImage imagemCertificado = criarImagemCertificado();
            desenharTextoNaImagem(imagemCertificado, aluno.getNome(), aluno.getFaixa(), dadosCertificado.getDataEvento().format(DateTimeFormatter.ofPattern(FORMATO_DATA)));

            try {
                salvarCertificadoEmJpg(imagemCertificado, caminhoPastaProfessor, aluno.getNome());
            } catch (IOException e) {
                throw new RuntimeException("Erro ao salvar o certificado!", e);
            }
        });
    }

    private BufferedImage criarImagemCertificado() {
        try {
            BufferedImage imagemFundo = ImageIO.read(new File(backgroundImagePath));
            BufferedImage imagemCertificado = new BufferedImage(LARGURA_CERTIFICADO, ALTURA_CERTIFICADO, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = imagemCertificado.createGraphics();
            g2d.drawImage(imagemFundo, 0, 0, LARGURA_CERTIFICADO, ALTURA_CERTIFICADO, null);
            configurarQualidadeGrafica(g2d);
            return imagemCertificado;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar a imagem do certificado!", e);
        }
    }

    private void configurarQualidadeGrafica(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    //TODO: AJUSTAR TAMANHO DA FONTE E LOCALIZAÇÃO DA DATA
    private void desenharTextoNaImagem(BufferedImage imagemCertificado, String nome, String faixa, String dataEvento) {
        Graphics2D g2d = imagemCertificado.createGraphics();
        configurarQualidadeGrafica(g2d);

        g2d.setColor(Color.BLACK);

        g2d.setFont(new Font(FONTE_PADRAO, Font.ITALIC, TAMANHO_FONTE_PADRAO));
        g2d.drawString(nome, 1460, 1024);
        g2d.drawString("Faixa " + faixa, 1452, 1312);

        g2d.setFont(new Font(FONTE_DATA, Font.ITALIC, TAMANHO_FONTE_DATA));
        g2d.drawString(dataEvento, 1452, 1600);

        g2d.dispose();
    }

    private void criarDiretorioSeNecessario(String caminhoPasta) {
        File diretorio = new File(caminhoPasta);
        if (!diretorio.exists()) {
            if (!diretorio.mkdirs()) {
                throw new RuntimeException("Erro ao criar o diretório: " + caminhoPasta);
            }
        }
    }

    @Override
    public void processarImagemDeFundo(MultipartFile imagemFundo) {
        try {
            Path tempFile = Files.createTempFile("background", ".jpg");
            Files.copy(imagemFundo.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            backgroundImagePath = tempFile.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar a imagem de fundo!", e);
        }
    }

    @Override
    public void salvarCertificadoEmJpg(BufferedImage imagemCertificado, String caminhoPasta, String nomeAluno) throws IOException {
        File outputFile = new File(caminhoPasta + "/" + nomeAluno + ".jpg");
        ImageIO.write(imagemCertificado, "jpg", outputFile);
    }
}
