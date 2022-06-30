package gerenciamentoDeAcademia.entidades;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TurmaTeste {

    @Test
    public void horario_eh_obrigatorio(){
        List<String> dias = new ArrayList<>();
        dias.add("Segunda-feira");
        dias.add("Quarta-quarta");
        String especificacao = "Judô";
        String professor = "Lucas";

        try {
            new Turma(null, dias, especificacao, professor, null);
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Horário da turma é obrigatório!");
        }
    }

    @Test
    public void dias_de_aula_nao_pode_ser_nulo(){
        String horario = "19h";
        String especificacao = "Judô";
        String professor = "Lucas";

        try {
            new Turma(horario, null, especificacao, professor, null);
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Dias de aula são obrigatórios!");
        }
    }

    @Test
    public void dias_de_aula_nao_pode_ser_zero(){
        String horario = "19h";
        List<String> dias = new ArrayList<>();
        String especificacao = "Judô";
        String professor = "Lucas";

        try {
            new Turma(horario, null, especificacao, professor, null);
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Dias de aula são obrigatórios!");
        }
    }

    @Test
    public void especificacao_eh_obrigatorio(){
        String horario = "19h";
        List<String> dias = new ArrayList<>();
        dias.add("Segunda-feira");
        dias.add("Quarta-quarta");
        String professor = "Lucas";

        try {
            new Turma(horario, dias, null, professor, null);
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Especificação da turma é obrigatória!");
        }
    }

    @Test
    public void professor_eh_obrigatorio(){
        String horario = "19h";
        List<String> dias = new ArrayList<>();
        dias.add("Segunda-feira");
        dias.add("Quarta-quarta");
        String especificacao = "Judô";

        try {
            new Turma(horario, dias, especificacao, null, null);
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Professor para a turma é obrigatória!");
        }
    }
}
