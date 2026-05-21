package gerenciamentoDeAcademia.enums;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public enum PlanoInstituicaoTipo {
    TRIAL_7_DIAS(7, "Teste grátis 7 dias"),
    MENSAL(30, "Mensal"),
    TRIMESTRAL(90, "Trimestral"),
    SEMESTRAL(180, "Semestral"),
    ANUAL(365, "Anual");

    private final int dias;
    private final String descricao;

    PlanoInstituicaoTipo(int dias, String descricao) {
        this.dias = dias;
        this.descricao = descricao;
    }

    public LocalDate calcularFim(LocalDate inicio) {
        return inicio.plusDays(dias);
    }
}
