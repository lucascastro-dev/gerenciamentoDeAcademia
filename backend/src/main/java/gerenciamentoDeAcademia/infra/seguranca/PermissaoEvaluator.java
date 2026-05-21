package gerenciamentoDeAcademia.infra.seguranca;

import gerenciamentoDeAcademia.enums.PermissaoSistema;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component("permissaoEvaluator")
public class PermissaoEvaluator {

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

    public boolean possuiMaster(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_MASTER".equals(a.getAuthority()));
    }
}
