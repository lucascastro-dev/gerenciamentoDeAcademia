package gerenciamentoDeAcademia.infra.seguranca;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.PermissaoSistema;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

@Getter
public class UsuarioAutenticado implements UserDetails {

    private final Usuario usuario;
    private final Funcionario funcionario;
    private final Aluno aluno;
    private final Long instituicaoId;
    private final SituacaoCobranca situacaoCobranca;
    private final Collection<? extends GrantedAuthority> authorities;

    public UsuarioAutenticado(Usuario usuario, Funcionario funcionario) {
        this(usuario, funcionario, null, null, SituacaoCobranca.ATIVO);
    }

    public UsuarioAutenticado(Usuario usuario, Funcionario funcionario, Aluno aluno) {
        this(usuario, funcionario, aluno, null, SituacaoCobranca.ATIVO);
    }

    public UsuarioAutenticado(Usuario usuario, Funcionario funcionario, Aluno aluno,
                              Long instituicaoId, SituacaoCobranca situacaoCobranca) {
        this.usuario = usuario;
        this.funcionario = funcionario;
        this.aluno = aluno;
        this.instituicaoId = instituicaoId;
        this.situacaoCobranca = situacaoCobranca != null ? situacaoCobranca : SituacaoCobranca.ATIVO;
        this.authorities = montarAuthorities(usuario, funcionario, aluno);
    }

    public boolean isPlanoInstituicaoAtivo() {
        return situacaoCobranca.permiteAcesso();
    }

    public boolean isPortalAluno() {
        return usuario != null && usuario.getRole() == UserRole.ALUNO;
    }

    private Collection<? extends GrantedAuthority> montarAuthorities(Usuario usuario, Funcionario funcionario, Aluno aluno) {
        if (usuario != null && usuario.getRole() == UserRole.ALUNO) {
            var permissoes = EnumSet.of(
                    PermissaoSistema.ALUNO_PORTAL_DADOS,
                    PermissaoSistema.ALUNO_PORTAL_TURMAS,
                    PermissaoSistema.ALUNO_PORTAL_MENSALIDADES,
                    PermissaoSistema.ALUNO_PORTAL_PAGAMENTO,
                    PermissaoSistema.ALUNO_PORTAL_SENHA,
                    PermissaoSistema.ALUNO_PORTAL_PROGRAMACAO
            );
            var list = permissoes.stream()
                    .map(p -> new SimpleGrantedAuthority(p.getCodigo()))
                    .collect(Collectors.toList());
            list.add(new SimpleGrantedAuthority("ROLE_ALUNO"));
            list.add(new SimpleGrantedAuthority("ROLE_USER"));
            return list;
        }

        TipoFuncionario tipo = funcionario != null ? funcionario.getTipoFuncionario() : null;
        if (tipo != null && tipo.isUsuarioMaster()) {
            return java.util.List.of(
                    new SimpleGrantedAuthority("ROLE_MASTER"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }
        if (tipo == null) {
            return java.util.List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        var permissoes = TipoFuncionario.codigosPermissao(tipo, funcionario.getAreaTerceirizado()).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        permissoes.add(new SimpleGrantedAuthority("ROLE_USER"));
        return permissoes;
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getLogin();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
