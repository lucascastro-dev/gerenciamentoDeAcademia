package gerenciamentoDeAcademia.enums;

/**
 * Situação de adimplência com tolerância de 5 dias após o vencimento.
 */
public enum SituacaoCobranca {
    /** Em dia ou dentro do período antes do vencimento. */
    ATIVO,
    /** Vencido há até 5 dias — acesso liberado com alerta. */
    EM_TOLERANCIA,
    /** Após a tolerância — login e operações bloqueados. */
    BLOQUEADO;

    public boolean permiteAcesso() {
        return this != BLOQUEADO;
    }

    public boolean exibeAlerta() {
        return this == EM_TOLERANCIA;
    }
}
