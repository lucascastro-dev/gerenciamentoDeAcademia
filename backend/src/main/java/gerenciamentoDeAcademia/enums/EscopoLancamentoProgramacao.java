package gerenciamentoDeAcademia.enums;

import lombok.Getter;

@Getter
public enum EscopoLancamentoProgramacao {
    ALUNO("Aluno específico"),
    TURMA("Turma completa");

    private final String descricao;

    EscopoLancamentoProgramacao(String descricao) {
        this.descricao = descricao;
    }
}
