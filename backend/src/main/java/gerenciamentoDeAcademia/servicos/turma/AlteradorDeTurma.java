package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IAlteradorDeTurma;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlteradorDeTurma implements IAlteradorDeTurma {
    TurmaRepository turmaRepository;
    FuncionarioRepository funcionarioRepository;
    AlunoRepository alunoRepository;

    @Override
    public void alterarTurma(Turma turmaParaAlterar) {
        Optional<Turma> turma = turmaRepository.findById(turmaParaAlterar.getId());
        ExcecaoDeDominio.quandoNulo(turma, "Turma não encontrada na base");

        ExcecaoDeDominio.quando(turma.get().getModalidade() != turmaParaAlterar.getModalidade(), "Não é possível alterar a modalidade da turma");

        if (turmaParaAlterar.getProfessor().getCpf() != turma.get().getProfessor().getCpf()) {
            ExcecaoDeDominio.quandoNulo(funcionarioRepository.findByCpf(turmaParaAlterar.getProfessor().getCpf()), "Professor não encontrado na base");
            turma.get().setProfessor(turmaParaAlterar.getProfessor());
        }

        turma.get().setDias(turmaParaAlterar.getDias());
        turma.get().setHorario(turmaParaAlterar.getHorario());

        turmaRepository.save(turma.get());
    }

    @Override
    public void adicionarAlunoNaTurma(Turma turmaParaAlterar) {
        Optional<Turma> turma = turmaRepository.findById(turmaParaAlterar.getId());
        ExcecaoDeDominio.quandoNulo(turma, "Turma não encontrada na base");

        for (Aluno aluno : turmaParaAlterar.getAlunos()) {
            ExcecaoDeDominio.quandoNulo(alunoRepository.findByCpf(aluno.getCpf()), "Aluno não encontrado na base");
            ExcecaoDeDominio.quando(turma.get().getAlunos().contains(aluno), String.format("Aluno %s já matriculado na turma", aluno.getNome()));

            turma.get().getAlunos().add(aluno);
        }

        turmaRepository.save(turma.get());
    }

    @Override
    public void removerAlunoNaTurma(Turma turmaParaAlterar) {
        Optional<Turma> turma = turmaRepository.findById(turmaParaAlterar.getId());
        ExcecaoDeDominio.quandoNulo(turma, "Turma não encontrada na base");

        for (Aluno aluno : turmaParaAlterar.getAlunos()) {
            ExcecaoDeDominio.quandoNulo(alunoRepository.findByCpf(aluno.getCpf()), "Aluno não encontrado na base");
            ExcecaoDeDominio.quando(!turma.get().getAlunos().contains(aluno), "Aluno não matriculado na turma");

            turma.get().getAlunos().remove(aluno);
        }

        turmaRepository.save(turma.get());
    }
}
