package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.DadosCertificadoDto;
import gerenciamentoDeAcademia.servicos.interfaces.IGeradorDeCertificados;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class GeradorDeCertificados implements IGeradorDeCertificados {
    private static final String CAMINHO_BASE = "C:/certificado";
    private static final String FORMATO_DATA = "dd/MM/yyyy";
    private static final Font FONTE_PADRAO = new Font("Brush Script MT", Font.ITALIC, 600);
    private static final Font FONTE_DATA = new Font("Arial", Font.ITALIC, 250);

    @Override
    public void gerarCertificado(DadosCertificadoDto dadosCertificado) {
        if (dadosCertificado == null) {
            throw new IllegalArgumentException("Dados do certificado não podem ser nulos.");
        }

        String caminhoPastaProfessor = criarDiretorioProfessor(dadosCertificado.getProfessor());
        BufferedImage imagemBase = processarImagemBase(dadosCertificado);
        BufferedImage imagemAlta = criarImagemAltaResolucao(imagemBase);

        dadosCertificado.getAlunos().forEach(aluno -> {
            BufferedImage imagemCertificado = copiarImagem(imagemAlta);
            adicionarInformacoesAluno(imagemCertificado, aluno.getNome(), aluno.getFaixa(), dadosCertificado.getDataEvento());
            incluirDesenhoDaCorFaixa(imagemCertificado, aluno.getFaixa());
            salvarCertificado(imagemCertificado, caminhoPastaProfessor, aluno.getNome());
        });
    }

    private String criarDiretorioProfessor(String nomeProfessor) {
        String caminhoPasta = CAMINHO_BASE + "/" + nomeProfessor;
        File diretorio = new File(caminhoPasta);
        if (!diretorio.exists() && !diretorio.mkdirs()) {
            throw new RuntimeException("Erro ao criar o diretório: " + caminhoPasta);
        }
        return caminhoPasta;
    }

    private BufferedImage processarImagemBase(DadosCertificadoDto dadosCertificado) {
        BufferedImage imagemBase = carregarImagemBase();
        if (dadosCertificado.getPersonalizado()) {
            imagemBase = adicionarLogoPersonalizada(imagemBase, dadosCertificado.getProjeto());
        }
        adicionarDataEvento(imagemBase, dadosCertificado.getDataEvento());
        return imagemBase;
    }

    private BufferedImage carregarImagemBase() {
        try {
            // Carregar a imagem base em alta resolução
            BufferedImage imagemBase = ImageIO.read(new File(CAMINHO_BASE + "/default.jpg"));
            if (imagemBase == null) {
                throw new IOException("Imagem base não encontrada ou inválida.");
            }
            // Certifique-se de que a imagem base tem alta resolução para impressão
            if (imagemBase.getWidth() < 3508 || imagemBase.getHeight() < 2480) {
                throw new RuntimeException("A imagem base não tem resolução suficiente para impressão.");
            }
            return imagemBase;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar a imagem base do certificado.", e);
        }
    }


    private BufferedImage adicionarLogoPersonalizada(BufferedImage imagem, String projeto) {
        try {
            BufferedImage logo = ImageIO.read(new File(CAMINHO_BASE + "/projetos" + projeto + ".png"));
            Graphics2D g2d = imagem.createGraphics();
            configurarQualidadeGrafica(g2d);
            g2d.drawImage(logo, 100, 100, null); // Coordenadas podem ser ajustadas conforme necessário
            g2d.dispose();
            return imagem;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao adicionar a logo personalizada.", e);
        }
    }

    private void adicionarDataEvento(BufferedImage imagem, LocalDate dataEvento) {
        Graphics2D g2d = imagem.createGraphics();
        configurarQualidadeGrafica(g2d);
        g2d.setFont(FONTE_DATA);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Rio de Janeiro, ".concat(dataEvento.format(DateTimeFormatter.ofPattern(FORMATO_DATA))), 6210, 7770);
        g2d.dispose();
    }

    private BufferedImage copiarImagem(BufferedImage original) {
        BufferedImage copia = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        Graphics g = copia.getGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return copia;
    }

    private void adicionarInformacoesAluno(BufferedImage imagem, String nome, String faixa, LocalDate dataEvento) {
        Graphics2D g2d = imagem.createGraphics();
        configurarQualidadeGrafica(g2d);
        g2d.setFont(FONTE_PADRAO);
        g2d.setColor(Color.BLACK);
        g2d.drawString(nome, 6160, 4230);
        g2d.drawString("Faixa " + faixa, 5200, 5425);
        g2d.dispose();
    }

    private void incluirDesenhoDaCorFaixa(BufferedImage imagemCertificadoPadrao, String faixa) {
        String caminhoImagemFaixa = CAMINHO_BASE.concat("/faixas/" + faixa + ".png");
        try {
            BufferedImage imagemFaixa = ImageIO.read(new File(caminhoImagemFaixa));
            Graphics2D g2d = imagemCertificadoPadrao.createGraphics();
            configurarQualidadeGrafica(g2d);

            // Ajuste as coordenadas (x, y) e o tamanho da faixa conforme necessário
            int x = 11580; // lateral
            int y = 1510; // horizontal
            int largura = 1695;
            int altura = 1250;

            g2d.drawImage(imagemFaixa, x, y, largura, altura, null);
            g2d.dispose();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar a imagem da faixa: " + caminhoImagemFaixa, e);
        }
    }

    private void configurarQualidadeGrafica(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private void salvarCertificado(BufferedImage imagem, String caminhoPasta, String nomeAluno) {
        try {
            File arquivo = new File(caminhoPasta + "/" + nomeAluno + ".jpg");

            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();

            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(1.0f); // 1.0f = qualidade máxima

            BufferedImage imagemCertificado = ajustarResolucaoImagem(imagem, 3508, 2480);
            try (FileImageOutputStream output = new FileImageOutputStream(arquivo)) {
                writer.setOutput(output);
                writer.write(null, new IIOImage(imagemCertificado, null, null), param);
            } finally {
                writer.dispose();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar o certificado.", e);
        }
    }

    private BufferedImage ajustarResolucaoImagem(BufferedImage original, int largura, int altura) {
        BufferedImage novaImagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = novaImagem.createGraphics();
        configurarQualidadeGrafica(g2d);
        g2d.drawImage(original, 0, 0, largura, altura, null);
        g2d.dispose();
        return novaImagem;
    }

    private BufferedImage criarImagemAltaResolucao(BufferedImage original) {
        int largura = original.getWidth();
        int altura = original.getHeight();
        BufferedImage imagemAltaResolucao = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = imagemAltaResolucao.createGraphics();
        configurarQualidadeGrafica(g2d);

        g2d.drawImage(original, 0, 0, largura, altura, null);
        g2d.dispose();

        return imagemAltaResolucao;
    }
}
