package gerenciamentoDeAcademia.auditoria;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditoriaRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        RevisaoAuditoria revisao = (RevisaoAuditoria) revisionEntity;
        revisao.setUsuarioLogin(obterLoginAtual());
    }

    private String obterLoginAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "sistema";
        }
        String nome = auth.getName();
        if (nome == null || nome.isBlank() || "anonymousUser".equals(nome)) {
            return "sistema";
        }
        return nome;
    }
}
