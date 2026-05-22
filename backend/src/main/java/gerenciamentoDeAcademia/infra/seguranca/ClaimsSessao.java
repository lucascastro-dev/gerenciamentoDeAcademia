package gerenciamentoDeAcademia.infra.seguranca;

import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import lombok.Getter;

@Getter
public class ClaimsSessao {
    private final String login;
    private final Long instituicaoId;
    private final SituacaoCobranca situacaoCobranca;
    private final boolean master;
    private final boolean portalAluno;

    public ClaimsSessao(String login, Long instituicaoId, SituacaoCobranca situacaoCobranca,
                        boolean master, boolean portalAluno) {
        this.login = login;
        this.instituicaoId = instituicaoId;
        this.situacaoCobranca = situacaoCobranca != null ? situacaoCobranca : SituacaoCobranca.ATIVO;
        this.master = master;
        this.portalAluno = portalAluno;
    }

    public boolean permiteAcessoOperacional() {
        return situacaoCobranca.permiteAcesso();
    }
}
