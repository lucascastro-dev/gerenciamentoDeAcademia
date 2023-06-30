package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.TurmaDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IMontadorDeTurma;
import gerenciamentoDeAcademia.utils.ExcecaoDeDominio;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Component
@Service
public class MontadorDeTurma implements IMontadorDeTurma {

    @Autowired
    private TurmaRepository turmaRepository;
    @Autowired
    private FuncionarioRepository funcionarioRepository;
    @Autowired
    private AlunoRepository alunoRepository;

    @Override
    public Turma montar(TurmaDto turmaDto) {
        validar(turmaDto);

        var turmaMontada = Turma.builder()
                .horario(turmaDto.getHorario())
                .dias(turmaDto.getDias())
                .modalidade(turmaDto.getModalidade())
                .build();

        Set<Aluno> alunosExistentes = new HashSet<>();

        if (turmaDto.getAlunos() != null && !turmaDto.getAlunos().isEmpty()) {
            for (Aluno aluno : turmaDto.getAlunos()) {
                Optional<Aluno> optionalAluno = Optional.ofNullable(alunoRepository.findByCpf(aluno.getCpf()));
                Aluno alunoExistente = optionalAluno.orElseThrow(() -> new RuntimeException("Aluno não encontrado: " + aluno.getCpf()));
                alunosExistentes.add(alunoExistente);
            }
        }

        turmaMontada.setAlunos(alunosExistentes);

        String professorCpf = turmaDto.getProfessor().getCpf();
        Optional<Funcionario> optionalProfessor = Optional.ofNullable(funcionarioRepository.findByCpf(professorCpf));

        if (optionalProfessor.isPresent()) {
            Funcionario professorExistente = optionalProfessor.get();
            turmaMontada.setProfessor(professorExistente);
        } else {
            throw new RuntimeException("Funcionario não encontrado");
        }

        return turmaRepository.save(turmaMontada);
    }

    public void validar(TurmaDto turmaDto) {
        ExcecaoDeDominio.quandoTextoVazioOuNulo(turmaDto.getHorario(), "Horário da turma é obrigatório!");
        ExcecaoDeDominio.quandoListaNulaOuVazia(turmaDto.getDias(), "Dias de aula são obrigatórios!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(turmaDto.getModalidade(), "Especificação da turma é obrigatória!");
        ExcecaoDeDominio.quandoNulo(turmaDto.getProfessor(), "Professor para a turma é obrigatória!");
    }
}