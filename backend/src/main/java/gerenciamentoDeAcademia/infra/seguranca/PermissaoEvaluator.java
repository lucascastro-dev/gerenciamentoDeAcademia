package gerenciamentoDeAcademia.infra.seguranca;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component("permissaoEvaluator")
public class PermissaoEvaluator {

    public boolean possuiAlguma(Authentication authentication, String... codigos) {
        if (codigos == null) {
            return false;
        }
        for (String codigo : codigos) {
            if (possui(authentication, codigo)) {
                return true;
            }
        }
        return false;
    }

    public boolean possui(Authentication authentication, String codigoPermissao) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (possuiMaster(authentication)) {
            return true;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(codigoPermissao));
    }

    /** Operador master da plataforma (CPF raiz ou sub-master delegado). */
    public boolean possuiMaster(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_MASTER".equals(a.getAuthority()));
    }

    /** Apenas o CPF configurado em app.master.cpf (não inclui sub-masters). */
    public boolean possuiMasterRaiz(Authentication authentication) {
        if (!(authentication != null && authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            return false;
        }
        return usuario.isMasterRaiz();
    }
}
