package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.entidades.TurmaMontada;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MontadorDeTurma implements IMontadorDeTurma {

    @Override
    public TurmaMontada montar(Turma turma) {
        var turmaMontada = new TurmaMontada();
        turmaMontada.setHorario(turma.getHorario());
        turmaMontada.setDias(turma.getDias());
        turmaMontada.setEspecificacao(turma.getEspecificacao());
        turmaMontada.setProfessor(turma.getProfessor());
        turmaMontada.setAlunos(turma.getAlunos());

        return turmaMontada;
    }
}