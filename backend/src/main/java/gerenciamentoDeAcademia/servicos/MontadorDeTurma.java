package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.dto.TurmaDto;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
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
    private ConsultaDeFuncionario consultaDeFuncionario;
    @Autowired
    private ConsultaDeAlunos consultaDeAlunos;

    @Override
    public Turma montar(TurmaDto turmaDto) {
        validar(turmaDto);
        turmaDto.setAlunos(validarAlunosNaTurma(turmaDto.getAlunos()));
        validarProfessor(turmaDto.getCpfProfessor());

        return turmaRepository.save(new Turma(turmaDto));
    }

    private void validarProfessor(String cpfProfessor) {
        try {
            consultaDeFuncionario.consultarFuncionarioPorCpf(cpfProfessor);
        } catch (NullPointerException e) {
            throw new ExcecaoDeDominio("Funcionario não encontrado");
        }
    }

    private List<AlunoDto> validarAlunosNaTurma(List<AlunoDto> alunos) {
        try {
            List<AlunoDto> alunosExistentes = new ArrayList<>();

            for (AlunoDto aluno : alunos) {
                AlunoDto alunoExistente = consultaDeAlunos.consultaAlunoPorCpf(aluno.getCpf());
                alunosExistentes.add(alunoExistente);
            }

            return alunosExistentes;

        } catch (NullPointerException e) {
            throw new RuntimeException("Aluno não encontrado");
        }
    }

    public void validar(TurmaDto turmaDto) {
        ExcecaoDeDominio.quandoNuloOuVazio(turmaDto.getHorario(), "Horário da turma é obrigatório!");
        ExcecaoDeDominio.quandoListaNulaOuVazia(turmaDto.getDias(), "Dias de aula são obrigatórios!");
        ExcecaoDeDominio.quandoNuloOuVazio(turmaDto.getModalidade(), "Especificação da turma é obrigatória!");
        ExcecaoDeDominio.quandoNulo(turmaDto.getCpfProfessor(), "Professor para a turma é obrigatória!");
    }
}