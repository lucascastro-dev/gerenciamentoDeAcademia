package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.dto.TurmaDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IMontadorDeTurma;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
        validarProfessor(turmaDto.getCpfProfessor());
        turmaDto.setAlunos(validarAlunosNaTurma(turmaDto.getAlunos()));

        return turmaRepository.save(new Turma(turmaDto));
    }

    private void validarProfessor(String cpfProfessor) {
        ExcecaoDeDominio.quandoNulo(funcionarioRepository.findByCpf(cpfProfessor), "Funcionario não encontrado");
    }

    private List<Aluno> validarAlunosNaTurma(List<Aluno> alunos) {
        List<Aluno> alunosExistentes = new ArrayList<>();

        for (Aluno aluno : alunos) {
            Aluno alunoEncontrado = alunoRepository.findByCpf(aluno.getCpf());
            ExcecaoDeDominio.quandoNulo(alunoEncontrado, "Aluno não encontrado");
            alunosExistentes.add(alunoEncontrado);
        }

        return alunosExistentes;
    }
}