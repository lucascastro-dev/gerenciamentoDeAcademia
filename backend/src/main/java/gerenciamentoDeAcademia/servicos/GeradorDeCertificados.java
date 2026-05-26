package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.DadosCertificadoDto;
import gerenciamentoDeAcademia.servicos.interfaces.IGeradorDeCertificados;
import org.springframework.core.io.ClassPathResource;
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

@Service
public class GeradorDeCertificados implements IGeradorDeCertificados {

    private static final String PASTA_TEMPLATES_CLASSPATH = "certificados/";

    private final String caminhoSaida;

    public GeradorDeCertificados(@org.springframework.beans.factory.annotation.Value("${app.certificado.base-path}") String caminhoSaida) {
        this.caminhoSaida = caminhoSaida;
    }

    private static final Set<String> CONECTIVOS = Set.of("de", "da", "do", "dos", "das", "e");

    private static final String FORMATO_DATA = "dd/MM/yyyy";
    private final Font FONTE_PADRAO = carregarFonte("/fonts/BrushScriptMT.ttf", 130f);
    private final Font FONTE_DATA = carregarFonte("/fonts/Arial.ttf", 70f);
    private final Font FONT_TEXT = carregarFonte("/fonts/TimesNewRoman.ttf", 105f);

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
            adicionarInformacoesAluno(imagemCertificado, aluno.getNome(), aluno.getFaixa());
            salvarCertificado(imagemCertificado, caminhoPastaProfessor, aluno.getNome());
        });

        gerarResumoDeFaixas(dadosCertificado, caminhoPastaProfessor);
    }

    private String criarDiretorioProfessor(String nomeProfessor) {
        String caminhoPasta = caminhoSaida + File.separator + nomeProfessor;
        File diretorio = new File(caminhoPasta);
        if (!diretorio.exists() && !diretorio.mkdirs()) {
            throw new RuntimeException("Erro ao criar o diretório: " + caminhoPasta);
        }
        return caminhoPasta;
    }

    private Font carregarFonte(String caminho, float tamanho) {
        try (InputStream is = getClass().getResourceAsStream(caminho)) {

            if (is == null) {
                throw new RuntimeException("Fonte não encontrada: " + caminho);
            }

            return Font.createFont(Font.TRUETYPE_FONT, is)
                    .deriveFont(tamanho);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar fonte", e);
        }
    }

    private BufferedImage processarImagemBase(DadosCertificadoDto dadosCertificado) {
        BufferedImage imagemBase = carregarImagemBase(dadosCertificado);
        adicionarDataEvento(imagemBase, dadosCertificado.getDataEvento());
        return imagemBase;
    }

    private BufferedImage carregarImagemBase(DadosCertificadoDto dadosCertificado) {
        String nomeArquivo = Boolean.TRUE.equals(dadosCertificado.getPersonalizado())
                ? dadosCertificado.getProjeto() + ".jpg"
                : "default.jpg";
        String recursoClasspath = PASTA_TEMPLATES_CLASSPATH + nomeArquivo;

        try {
            ClassPathResource resource = new ClassPathResource(recursoClasspath);
            if (!resource.exists()) {
                throw new IOException("Template não encontrado no classpath: " + recursoClasspath);
            }
            try (InputStream input = resource.getInputStream()) {
                BufferedImage imagemBase = ImageIO.read(input);
                if (imagemBase == null) {
                    throw new IOException("Imagem base inválida: " + recursoClasspath);
                }
                if (imagemBase.getWidth() < 3508 || imagemBase.getHeight() < 2480) {
                    throw new RuntimeException("A imagem base não tem resolução suficiente para impressão.");
                }
                return imagemBase;
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Erro ao carregar a imagem base do certificado (" + recursoClasspath + ").", e);
        }
    }

    private void adicionarDataEvento(BufferedImage imagem, LocalDate dataEvento) {
        Graphics2D g2d = imagem.createGraphics();
        configurarQualidadeGrafica(g2d);
        g2d.setFont(FONTE_DATA);
        g2d.setColor(Color.BLACK);
        String textoData = "Rio de Janeiro, " + dataEvento.format(DateTimeFormatter.ofPattern(FORMATO_DATA));
        FontMetrics metrics = g2d.getFontMetrics();
        int larguraTexto = metrics.stringWidth(textoData);
        int xData = (imagem.getWidth() - larguraTexto) / 2;
        int yData = (int) (16 * 300 / 2.54);

        g2d.drawString(textoData, xData, yData);
        g2d.dispose();
    }

    private BufferedImage copiarImagem(BufferedImage original) {
        BufferedImage copia = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        Graphics g = copia.getGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return copia;
    }

    private void adicionarInformacoesAluno(BufferedImage imagem, String nome, String faixa) {
        Graphics2D g2d = imagem.createGraphics();
        configurarQualidadeGrafica(g2d);
        g2d.setColor(Color.BLACK);

        int xMin = (int) (3 * 300 / 2.54);
        int xMax = (int) (27 * 300 / 2.54);
        int larguraDisponivel = xMax - xMin;

        g2d.setFont(FONT_TEXT);
        FontMetrics metricsTextoInicial = g2d.getFontMetrics(FONT_TEXT);
        String textoInicial = "Certificamos que";
        int larguraTextoInicial = metricsTextoInicial.stringWidth(textoInicial);

        g2d.setFont(FONTE_PADRAO);
        FontMetrics metricsNome = g2d.getFontMetrics(FONTE_PADRAO);
        nome = ajustarNomeParaCabecalho(nome, metricsNome, larguraTextoInicial, larguraDisponivel);

        int larguraNome = metricsNome.stringWidth(nome);

        int larguraTotal = larguraTextoInicial + larguraNome + 25;

        int xInicial = xMin + (larguraDisponivel - larguraTotal) / 2;

        int xNome = xInicial + larguraTextoInicial + 25;

        int ajusteVertical = (int) (1.8 * 300 / 2.54);

        int yCentral = (imagem.getHeight() / 2) - ajusteVertical;

        g2d.setFont(FONT_TEXT);
        g2d.drawString(textoInicial, xInicial, yCentral);

        g2d.setFont(FONTE_PADRAO);
        g2d.drawString(nome, xNome, yCentral);

        Font fonteFaixa = carregarFonte("/fonts/BrushScriptMT.ttf", 160f);
        g2d.setFont(fonteFaixa);
        FontMetrics metrics = g2d.getFontMetrics(fonteFaixa);
        String textoFaixa = "Faixa " + faixa;

        int larguraTexto = metrics.stringWidth(textoFaixa);
        int xFaixa = (imagem.getWidth() - larguraTexto) / 2;
        int yFaixa = (int) (11.08 * 300 / 2.54);
        g2d.drawString(textoFaixa, xFaixa, yFaixa);

        g2d.dispose();
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

    private void gerarResumoDeFaixas(DadosCertificadoDto dadosCertificado, String caminhoPasta) {
        Map<String, Map<String, Integer>> resumo = new HashMap<>();

        dadosCertificado.getAlunos().forEach(aluno -> {
            String faixa = aluno.getFaixa();
            String tamanho = aluno.getMedida();

            resumo.computeIfAbsent(faixa, k -> new HashMap<>())
                    .merge(tamanho, 1, Integer::sum);
        });

        StringBuilder conteudo = new StringBuilder();
        resumo.forEach((faixa, tamanhos) -> {
            conteudo.append(faixa).append(":\n");
            tamanhos.forEach((tamanho, quantidade) ->
                    conteudo.append(tamanho).append(" - ").append(quantidade).append(" unidades\n")
            );
            conteudo.append("\n");
        });

        Path caminhoArquivo;

        if (dadosCertificado.getPersonalizado()) {
            caminhoArquivo = Path.of(caminhoPasta,
                    "pedido_faixas_".concat(dadosCertificado.getProfessor()).concat("_").concat(dadosCertificado.getProjeto()).concat(".txt"));
        } else {
            caminhoArquivo = Path.of(caminhoPasta,
                    "pedido_faixas_".concat(dadosCertificado.getProfessor()).concat(".txt"));
        }

        try {
            Files.writeString(caminhoArquivo, conteudo.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar o resumo de faixas.", e);
        }
    }

    private String ajustarNomeParaCabecalho(String nome, FontMetrics metricsNome, int larguraTextoInicial, int larguraDisponivel) {
        String nomeAtual = nome;
        int larguraNome = metricsNome.stringWidth(nomeAtual);
        int larguraTotal = larguraTextoInicial + larguraNome + 25;

        if (larguraTotal <= larguraDisponivel)
            return nomeAtual;

        List<String> partes = new ArrayList<>(List.of(nome.split("\\s+")));

        for (int i = 1; i < partes.size() - 1; i++) {
            String parte = partes.get(i);

            if (CONECTIVOS.contains(parte.toLowerCase()))
                continue;

            if (parte.length() <= 2 || parte.endsWith("."))
                continue;

            partes.set(i, parte.charAt(0) + ".");

            nomeAtual = String.join(" ", partes);
            larguraNome = metricsNome.stringWidth(nomeAtual);
            larguraTotal = larguraTextoInicial + larguraNome + 25;

            if (larguraTotal <= larguraDisponivel)
                return nomeAtual;
        }

        throw new IllegalArgumentException(
                "Nome excede os limites horizontais permitidos mesmo abreviado: "
                        + nome
        );
    }
}
