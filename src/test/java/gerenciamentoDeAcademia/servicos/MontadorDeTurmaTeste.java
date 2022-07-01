package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Turma;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class MontadorDeTurmaTeste {

    @Test
    public void deve_montar_turma() {
        var turma = new Turma();
        List<String> lista = new ArrayList<>();
        lista.add("Segunda");
        lista.add("Quarta");
        List<Aluno> alunos = new ArrayList<>();
        turma.setHorario("20:00");
        turma.setDias(lista);
        turma.setEspecificacao("Spinning");
        turma.setProfessor(new Funcionario());
        turma.setAlunos(alunos);
        var montadorDeTurma = new MontadorDeTurma();

        var turmaMontada = montadorDeTurma.montar(turma);

        Assertions.assertEquals(turmaMontada.getHorario(), turma.getHorario());
        Assertions.assertEquals(turmaMontada.getDias(), turma.getDias());
        Assertions.assertEquals(turmaMontada.getEspecificacao(), turma.getEspecificacao());
        Assertions.assertEquals(turmaMontada.getProfessor(), turma.getProfessor());
        Assertions.assertEquals(turmaMontada.getAlunos(), turma.getAlunos());
    }
}