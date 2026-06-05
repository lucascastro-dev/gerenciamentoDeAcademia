package gerenciamentoDeAcademia.infra.seguranca;

import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import lombok.Getter;

@Getter
public class ClaimsSessao {
    private final String login;
    private final Long instituicaoId;
    private final SituacaoCobranca situacaoCobranca;
    private final StatusFinanceiroInstituicao statusFinanceiro;
    private final boolean operadorPlataforma;
    private final boolean masterRaiz;
    private final boolean portalAluno;

    public ClaimsSessao(String login, Long instituicaoId, SituacaoCobranca situacaoCobranca,
                        StatusFinanceiroInstituicao statusFinanceiro,
                        boolean operadorPlataforma, boolean masterRaiz, boolean portalAluno) {
        this.login = login;
        this.instituicaoId = instituicaoId;
        this.situacaoCobranca = situacaoCobranca != null ? situacaoCobranca : SituacaoCobranca.ATIVO;
        this.statusFinanceiro = statusFinanceiro != null
                ? statusFinanceiro
                : StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO;
        this.operadorPlataforma = operadorPlataforma;
        this.masterRaiz = masterRaiz;
        this.portalAluno = portalAluno;
    }

    /** Compatibilidade com tokens antigos sem status financeiro. */
    public ClaimsSessao(String login, Long instituicaoId, SituacaoCobranca situacaoCobranca,
                        boolean master, boolean portalAluno) {
        this(login, instituicaoId, situacaoCobranca, StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO,
                master, false, portalAluno);
    }

    public boolean permiteAcessoOperacional() {
        return situacaoCobranca.permiteAcesso();
    }
}
