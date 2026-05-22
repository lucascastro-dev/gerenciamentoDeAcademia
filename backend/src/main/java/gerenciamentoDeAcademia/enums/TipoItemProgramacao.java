package gerenciamentoDeAcademia.enums;

import lombok.Getter;

/** Tipo de item na programação do aluno (escola, academia, etc.). */
@Getter
public enum TipoItemProgramacao {
    PROVA("Prova / avaliação"),
    SERIE_TREINO("Série de treino"),
    AULA("Aula / matéria"),
    EVENTO("Evento / atividade");

    private final String descricao;

    TipoItemProgramacao(String descricao) {
        this.descricao = descricao;
    }
}
