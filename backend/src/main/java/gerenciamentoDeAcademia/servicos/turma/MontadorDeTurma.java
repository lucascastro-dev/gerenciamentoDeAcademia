package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.dto.TurmaDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
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
        validarCamposObrigatorios(turmaDto);
        Funcionario professor = resolverProfessorOpcional(turmaDto.getCpfProfessor());
        List<Aluno> alunos = validarAlunosNaTurma(turmaDto.getAlunos() != null ? turmaDto.getAlunos() : List.of());

        Turma turma = new Turma();
        turma.setHorario(turmaDto.getHorario());
        turma.setDias(turmaDto.getDias());
        turma.setModalidade(turmaDto.getModalidade());
        turma.setProfessor(professor);
        turma.setAlunos(new HashSet<>(alunos));

        return turmaRepository.save(turma);
    }

    private void validarCamposObrigatorios(TurmaDto turmaDto) {
        ExcecaoDeDominio.quandoNuloOuVazio(turmaDto.getHorario(), "Horário da turma é obrigatório");
        ExcecaoDeDominio.quandoListaNulaOuVazia(turmaDto.getDias(), "Dias de aula são obrigatórios");
        ExcecaoDeDominio.quandoNuloOuVazio(turmaDto.getModalidade(), "Modalidade da turma é obrigatória");
    }

    private Funcionario resolverProfessorOpcional(String cpfProfessor) {
        if (!StringUtils.hasText(cpfProfessor)) {
            return null;
        }
        String cpf = cpfProfessor.replaceAll("\\D", "");
        Funcionario professor = funcionarioRepository.findByCpf(cpf);
        ExcecaoDeDominio.quandoNulo(professor, "Funcionario não encontrado");
        return professor;
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
