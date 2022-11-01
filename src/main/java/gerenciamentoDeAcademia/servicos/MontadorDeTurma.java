package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.TurmaDto;
import gerenciamentoDeAcademia.entidades.TurmaMontada;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.utils.ExcecaoDeDominio;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Component
@Service
public class MontadorDeTurma implements IMontadorDeTurma {

    @Autowired
    private TurmaRepository turmaRepository;

    @Override
    public TurmaMontada montar(TurmaDto turmaDto) {
        validar(turmaDto);
        
        var turmaMontada = TurmaMontada.builder()
                .horario(turmaDto.getHorario())
                .dias(turmaDto.getDias())
                .especificacao(turmaDto.getEspecificacao())
                .professor(turmaDto.getProfessor())
                .alunos(turmaDto.getAlunos());

        return turmaRepository.save(turmaMontada.build());
    }

    public void validar(TurmaDto turmaDto){
        ExcecaoDeDominio.quandoTextoVazioOuNulo(turmaDto.getHorario(), "Horário da turma é obrigatório!");
        ExcecaoDeDominio.quandoListaNulaOuVazia(turmaDto.getDias(), "Dias de aula são obrigatórios!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(turmaDto.getEspecificacao(), "Especificação da turma é obrigatória!");
        ExcecaoDeDominio.quandoNulo(turmaDto.getProfessor(), "Professor para a turma é obrigatória!");
    }
}