package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.dto.TurmaDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class MontadorDeTurmaTesteDto {

    @Test
    public void deve_montar_turma() {
        var turma = new TurmaDto();
        List<String> lista = new ArrayList<>();
        lista.add("Segunda");
        lista.add("Quarta");
        List<AlunoDto> alunoDtos = new ArrayList<>();
        turma.setHorario("20:00");
        turma.setDias(lista);
        turma.setEspecificacao("Spinning");
        turma.setProfessor(new FuncionarioDto());
        turma.setAlunos(alunoDtos);
        var montadorDeTurma = new MontadorDeTurma();

        var turmaMontada = montadorDeTurma.montar(turma);

        Assertions.assertEquals(turmaMontada.getHorario(), turma.getHorario());
        Assertions.assertEquals(turmaMontada.getDias(), turma.getDias());
        Assertions.assertEquals(turmaMontada.getEspecificacao(), turma.getEspecificacao());
        Assertions.assertEquals(turmaMontada.getProfessor(), turma.getProfessor());
        Assertions.assertEquals(turmaMontada.getAlunos(), turma.getAlunos());
    }

    @Test
    public void horario_eh_obrigatorio(){
        List<String> dias = new ArrayList<>();
        dias.add("Segunda-feira");
        dias.add("Quarta-quarta");
        String especificacao = "Judô";
        FuncionarioDto professor = new FuncionarioDto();
        professor.setNome("Lucas");

        try {
            new TurmaDto(null, dias, especificacao, professor, null);
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Horário da turma é obrigatório!");
        }
    }

    @Test
    public void dias_de_aula_nao_pode_ser_nulo(){
        String horario = "19h";
        String especificacao = "Judô";
        FuncionarioDto professor = new FuncionarioDto();
        professor.setNome("Lucas");

        try {
            new TurmaDto(horario, null, especificacao, professor, null);
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Dias de aula são obrigatórios!");
        }
    }

    @Test
    public void dias_de_aula_nao_pode_ser_zero(){
        String horario = "19h";
        String especificacao = "Judô";
        FuncionarioDto professor = new FuncionarioDto();
        professor.setNome("Lucas");

        try {
            new TurmaDto(horario, null, especificacao, professor, null);
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
        FuncionarioDto professor = new FuncionarioDto();
        professor.setNome("Lucas");

        try {
            new TurmaDto(horario, dias, null, professor, null);
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
            new TurmaDto(horario, dias, especificacao, null, null);
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Professor para a turma é obrigatória!");
        }
    }}