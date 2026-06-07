package gerenciamentoDeAcademia.servicos.turma;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import gerenciamentoDeAcademia.dto.PresencaGradeDto;
import gerenciamentoDeAcademia.util.MascaramentoDadosUtil;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@Component
public class GeradorRelatorioPresencaPdf {

    public byte[] gerar(String instituicao, String professor, PresencaGradeDto grade) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font texto = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font cabecalho = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);

            document.add(new Paragraph("Relatório de presença", titulo));
            document.add(new Paragraph("Instituição: " + nullSafe(instituicao), texto));
            document.add(new Paragraph("Turma: " + nullSafe(grade.getModalidade()), texto));
            document.add(new Paragraph("Sala: " + nullSafe(grade.getSala()) + " · Horário: " + nullSafe(grade.getHorario()), texto));
            document.add(new Paragraph("Professor: " + nullSafe(professor), texto));
            String mesNome = Month.of(grade.getMes()).getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
            document.add(new Paragraph("Período: " + mesNome + "/" + grade.getAno(), texto));
            document.add(new Paragraph(" ", texto));

            int colunas = 2 + grade.getDiasComAula().size() + 4;
            PdfPTable table = new PdfPTable(colunas);
            table.setWidthPercentage(100f);

            adicionarCelula(table, "Aluno", cabecalho);
            adicionarCelula(table, "CPF", cabecalho);
            for (Integer dia : grade.getDiasComAula()) {
                adicionarCelula(table, String.valueOf(dia), cabecalho);
            }
            adicionarCelula(table, "P", cabecalho);
            adicionarCelula(table, "F", cabecalho);
            adicionarCelula(table, "J", cabecalho);
            adicionarCelula(table, "A", cabecalho);

            for (PresencaGradeDto.AlunoPresencaLinhaDto aluno : grade.getAlunos()) {
                adicionarCelula(table, nullSafe(aluno.getNome()), texto);
                adicionarCelula(table, MascaramentoDadosUtil.cpf(aluno.getCpf()), texto);
                for (Integer dia : grade.getDiasComAula()) {
                    adicionarCelula(table, aluno.getRegistros().getOrDefault(dia, ""), texto);
                }
                adicionarCelula(table, String.valueOf(aluno.getTotais().getOrDefault("P", 0)), texto);
                adicionarCelula(table, String.valueOf(aluno.getTotais().getOrDefault("F", 0)), texto);
                adicionarCelula(table, String.valueOf(aluno.getTotais().getOrDefault("J", 0)), texto);
                adicionarCelula(table, String.valueOf(aluno.getTotais().getOrDefault("A", 0)), texto);
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF de presença.", e);
        }
    }

    private void adicionarCelula(PdfPTable table, String valor, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(valor != null ? valor : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private String nullSafe(String valor) {
        return valor != null ? valor : "";
    }
}
