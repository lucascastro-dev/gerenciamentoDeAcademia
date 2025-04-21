package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Turma;

public interface IAlteradorDeTurma {
    void alterarTurma(Turma turmaParaAlterar);

    void adicionarAlunoNaTurma(Turma turmaParaAlterar);

    void removerAlunoNaTurma(Turma turmaParaAlterar);
}
