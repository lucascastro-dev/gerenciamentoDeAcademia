package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.entidades.TurmaMontada;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Component
@Service
public class MontadorDeTurma implements IMontadorDeTurma {

    @Override
    public TurmaMontada montar(Turma turma) {
        if (turma.getHorario() == null)
            throw new RuntimeException("Horário da turma é obrigatório!");

        if (turma.getDias() == null || turma.getDias().size() == 0)
            throw new RuntimeException("Dias de aula são obrigatórios!");

        if (turma.getEspecificacao() == null)
            throw new RuntimeException("Especificação da turma é obrigatória!");

        if (turma.getProfessor() == null)
            throw new RuntimeException("Professor para a turma é obrigatória!");
        
        var turmaMontada = new TurmaMontada();
        turmaMontada.setHorario(turma.getHorario());
        turmaMontada.setDias(turma.getDias());
        turmaMontada.setEspecificacao(turma.getEspecificacao());
        turmaMontada.setProfessor(turma.getProfessor());
        turmaMontada.setAlunos(turma.getAlunos());

        return turmaMontada;
    }
}