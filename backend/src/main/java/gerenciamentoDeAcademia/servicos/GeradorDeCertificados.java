package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.servicos.interfaces.IGeradorDeCertificados;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@Service
public class GeradorDeCertificados implements IGeradorDeCertificados {
    private String backgroundImagePath;

    @Override
    public void processarArquivoExcel(MultipartFile arquivo) {
        try {
            Workbook workbook = WorkbookFactory.create(arquivo.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String nome = row.getCell(0).getStringCellValue();

                if (nome == null || nome.trim().isEmpty()) {
                    break;
                }

                String faixa = "";
                if (row.getCell(1) != null) {
                    faixa = row.getCell(1).getStringCellValue();
                }

                BufferedImage imagemCertificado = gerarCertificado(nome, faixa);
                salvarCertificadoEmJpg(imagemCertificado, nome);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Erro de E/S ao ler o arquivo Excel!", ex);
        } catch (EncryptedDocumentException ex) {
            throw new RuntimeException("O arquivo Excel está criptografado!", ex);
        }
    }

    @Override
    public void processarImagemDeFundo(MultipartFile imagemFundo) {
        try {
            File tempFile = File.createTempFile("background", ".jpg");
            imagemFundo.transferTo(tempFile);

            backgroundImagePath = tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar a imagem de fundo!", e);
        }
    }

    @Override
    public BufferedImage gerarCertificado(String nome, String faixa) {
        int largura = 3508;
        int altura = 2480;

        BufferedImage imagemCertificado = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagemCertificado.createGraphics();

        try {
            BufferedImage imagemFundo = ImageIO.read(new File(backgroundImagePath));
            g2d.drawImage(imagemFundo, 0, 0, largura, altura, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Brush Script MT", Font.ITALIC, 150));

        String textoNome = nome;
        int xNome = 1460;
        int yNome = 1024;
        g2d.drawString(nome, xNome, yNome);

        String textoFaixa = "Preta" + faixa;
        int xFaixa = 1452;
        int yFaixa = 1312;
        g2d.drawString(textoFaixa, xFaixa, yFaixa);

        g2d.dispose();

        return imagemCertificado;
    }

    @Override
    public void salvarCertificadoEmJpg(BufferedImage imagemCertificado, String nome) throws IOException {
        File outputFile = new File(nome + ".jpg");
        ImageIO.write(imagemCertificado, "jpg", outputFile);
    }
}
