package gerenciamentoDeAcademia.infra.seguranca;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.AreaTerceirizado;
import gerenciamentoDeAcademia.enums.PermissaoSistema;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
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
    private final StatusFinanceiroInstituicao statusFinanceiroInstituicao;
    private final boolean operadorPlataforma;
    private final boolean masterRaiz;
    /** Perfil efetivo na instituição da sessão (vínculo), quando informado. */
    private final TipoFuncionario tipoPermissao;
    private final AreaTerceirizado areaPermissao;
    private final Collection<? extends GrantedAuthority> authorities;

    public UsuarioAutenticado(Usuario usuario, Funcionario funcionario) {
        this(usuario, funcionario, null, null, SituacaoCobranca.ATIVO, StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO,
                false, false, null, null);
    }

    public UsuarioAutenticado(Usuario usuario, Funcionario funcionario, Aluno aluno) {
        this(usuario, funcionario, aluno, null, SituacaoCobranca.ATIVO, StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO,
                false, false, null, null);
    }

    public UsuarioAutenticado(Usuario usuario, Funcionario funcionario, Aluno aluno,
                              Long instituicaoId, SituacaoCobranca situacaoCobranca) {
        this(usuario, funcionario, aluno, instituicaoId, situacaoCobranca,
                StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO, false, false, null, null);
    }

    public UsuarioAutenticado(Usuario usuario, Funcionario funcionario, Aluno aluno,
                              Long instituicaoId, SituacaoCobranca situacaoCobranca,
                              StatusFinanceiroInstituicao statusFinanceiroInstituicao,
                              boolean operadorPlataforma, boolean masterRaiz) {
        this(usuario, funcionario, aluno, instituicaoId, situacaoCobranca,
                statusFinanceiroInstituicao, operadorPlataforma, masterRaiz, null, null);
    }

    public UsuarioAutenticado(Usuario usuario, Funcionario funcionario, Aluno aluno,
                              Long instituicaoId, SituacaoCobranca situacaoCobranca,
                              StatusFinanceiroInstituicao statusFinanceiroInstituicao,
                              boolean operadorPlataforma, boolean masterRaiz,
                              TipoFuncionario tipoPermissao, AreaTerceirizado areaPermissao) {
        this.usuario = usuario;
        this.funcionario = funcionario;
        this.aluno = aluno;
        this.instituicaoId = instituicaoId;
        this.situacaoCobranca = situacaoCobranca != null ? situacaoCobranca : SituacaoCobranca.ATIVO;
        this.statusFinanceiroInstituicao = statusFinanceiroInstituicao != null
                ? statusFinanceiroInstituicao
                : StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO;
        this.operadorPlataforma = operadorPlataforma;
        this.masterRaiz = masterRaiz;
        this.tipoPermissao = tipoPermissao;
        this.areaPermissao = areaPermissao;
        this.authorities = montarAuthorities(usuario, funcionario, aluno, operadorPlataforma, tipoPermissao, areaPermissao);
    }

    public boolean isOperadorPlataforma() {
        return operadorPlataforma;
    }

    public boolean isMasterRaiz() {
        return masterRaiz;
    }

    public boolean acessoFinanceiroCompleto() {
        return operadorPlataforma
                || statusFinanceiroInstituicao == StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO
                || statusFinanceiroInstituicao == StatusFinanceiroInstituicao.NAO_APLICAVEL;
    }

    public boolean isPlanoInstituicaoAtivo() {
        return situacaoCobranca.permiteAcesso();
    }

    public boolean isPortalAluno() {
        return usuario != null && usuario.getRole() == UserRole.ALUNO;
    }

    public TipoFuncionario getTipoPermissao() {
        if (tipoPermissao != null) {
            return tipoPermissao;
        }
        return funcionario != null ? funcionario.getTipoFuncionario() : null;
    }

    public AreaTerceirizado getAreaPermissao() {
        if (areaPermissao != null) {
            return areaPermissao;
        }
        return funcionario != null ? funcionario.getAreaTerceirizado() : null;
    }

    private Collection<? extends GrantedAuthority> montarAuthorities(
            Usuario usuario, Funcionario funcionario, Aluno aluno, boolean operadorPlataforma,
            TipoFuncionario tipoPermissao, AreaTerceirizado areaPermissao) {
        if (operadorPlataforma) {
            return java.util.List.of(
                    new SimpleGrantedAuthority("ROLE_MASTER"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }
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

        TipoFuncionario tipo = tipoPermissao != null
                ? tipoPermissao
                : (funcionario != null ? funcionario.getTipoFuncionario() : null);
        if (tipo == null) {
            return java.util.List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        AreaTerceirizado area = areaPermissao != null ? areaPermissao : funcionario.getAreaTerceirizado();
        var permissoes = TipoFuncionario.codigosPermissao(tipo, area).stream()
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
