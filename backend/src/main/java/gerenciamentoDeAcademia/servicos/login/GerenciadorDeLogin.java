package gerenciamentoDeAcademia.servicos.login;

import gerenciamentoDeAcademia.dto.VinculoInstituicaoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.PermissaoSistema;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.TipoAcesso;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.enums.UserRole;
import gerenciamentoDeAcademia.excecao.ApplicationException;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.UsuarioRepository;
import gerenciamentoDeAcademia.servicos.instituicao.GerenciadorDeInstituicao;
import gerenciamentoDeAcademia.servicos.funcionario.ConsultaDeFuncionario;
import gerenciamentoDeAcademia.servicos.interfaces.IGerenciadorDeLogin;
import gerenciamentoDeAcademia.servicos.cobranca.ServicoSituacaoCobranca;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Service
public class GerenciadorDeLogin implements IGerenciadorDeLogin, UserDetailsService {

    @Autowired
    private ConsultaDeFuncionario consultaDeFuncionario;

    @Autowired
    private GerenciadorDeInstituicao gerenciadorDeInstituicao;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private ServicoSituacaoCobranca servicoSituacaoCobranca;

    public List<VinculoInstituicaoDto> listarInstituicoesPorCpf(String cpf) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "Informe o CPF");
        Funcionario funcionario = funcionarioRepository.findByCpf(cpf);
        if (funcionario != null) {
            return instituicaoRepository.findInstituicoesPorCpfFuncionario(cpf).stream()
                    .filter(a -> Boolean.TRUE.equals(a.getCadastroAtivo()))
                    .map(a -> new VinculoInstituicaoDto(a.getId(), a.getRazaoSocial()))
                    .toList();
        }
        Aluno aluno = alunoRepository.findByCpf(cpf);
        if (aluno != null) {
            return instituicaoRepository.findInstituicoesPorCpfAluno(cpf).stream()
                    .map(a -> new VinculoInstituicaoDto(a.getId(), a.getRazaoSocial()))
                    .toList();
        }
        return List.of();
    }

    public String solicitarRecuperacaoSenha(String cpf) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "Informe o CPF");
        Funcionario funcionario = funcionarioRepository.findByCpf(cpf);
        Aluno aluno = alunoRepository.findByCpf(cpf);
        ExcecaoDeDominio.quando(funcionario == null && aluno == null, "CPF não encontrado no sistema");
        if (funcionario != null) {
            ExcecaoDeDominio.quando(
                    instituicaoRepository.findInstituicoesPorCpfFuncionario(cpf).isEmpty(),
                    "Nenhum vínculo com instituição encontrado para este CPF");
        }
        return "Solicitação registrada. O envio de e-mail para redefinição de senha será implementado em breve.";
    }

    @Override
    public void validarLogin(String login, String vinculo) {
        Usuario usuario = repository.findByLogin(login);
        ExcecaoDeDominio.quandoNulo(usuario, "Usuário ou senha inválidos.");

        if (usuario.getRole() == UserRole.ALUNO) {
            validarLoginAluno(login, vinculo);
            validarCobrancaAluno(login);
            return;
        }

        Funcionario funcionario = consultaDeFuncionario.consultarFuncionarioPorCpf(login);
        ExcecaoDeDominio.quandoNulo(funcionario, "Usuário ou senha inválidos.");
        ExcecaoDeDominio.quando(!funcionario.getCadastroAtivo(),
                "Seu cadastro não está ativo. Entre em contato com a administração da instituição.");

        boolean ehVinculado = gerenciadorDeInstituicao.verificarVinculo(funcionario.getCpf(), vinculo);
        if (!ehVinculado) {
            throw new ExcecaoDeDominio("Usuário não possui vínculo com a instituição informada.");
        }
        if (!funcionario.isUsuarioMaster()) {
            validarCobrancaInstituicao(vinculo);
        }
    }

    private void validarCobrancaAluno(String cpf) {
        Aluno aluno = alunoRepository.findByCpf(cpf);
        SituacaoCobranca situacao = servicoSituacaoCobranca.situacaoMensalidadeAluno(aluno);
        if (situacao == SituacaoCobranca.BLOQUEADO) {
            throw new ApplicationException(
                    ServicoSituacaoCobranca.MSG_BLOQUEIO_ALUNO, HttpStatus.FORBIDDEN);
        }
    }

    private void validarCobrancaInstituicao(String vinculo) {
        try {
            SituacaoCobranca situacao = servicoSituacaoCobranca.situacaoPlanoInstituicao(Long.parseLong(vinculo));
            if (situacao == SituacaoCobranca.BLOQUEADO) {
                throw new ApplicationException(
                        ServicoSituacaoCobranca.MSG_BLOQUEIO_INSTITUICAO, HttpStatus.FORBIDDEN);
            }
        } catch (NumberFormatException e) {
            throw new ExcecaoDeDominio("Instituição inválida.");
        }
    }

    private void validarLoginAluno(String cpf, String vinculo) {
        Aluno aluno = alunoRepository.findByCpf(cpf);
        ExcecaoDeDominio.quandoNulo(aluno, "Usuário ou senha inválidos.");
        try {
            Long id = Long.parseLong(vinculo);
            ExcecaoDeDominio.quando(
                    instituicaoRepository.findById(id).filter(a -> Boolean.TRUE.equals(a.getCadastroAtivo())).isEmpty(),
                    "Instituição inválida ou inativa.");
            ExcecaoDeDominio.quando(
                    !instituicaoRepository.alunoVinculadoInstituicao(cpf, id),
                    "Você não está matriculado em turma desta instituição.");
        } catch (NumberFormatException e) {
            throw new ExcecaoDeDominio("Instituição inválida.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = repository.findByLogin(username);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
        if (usuario.getRole() == UserRole.ALUNO) {
            Aluno aluno = alunoRepository.findByCpf(username);
            return new UsuarioAutenticado(usuario, null, aluno);
        }
        Funcionario funcionario = funcionarioRepository.findByCpf(username);
        return new UsuarioAutenticado(usuario, funcionario);
    }

    public TipoAcesso resolverTipoAcesso(UsuarioAutenticado autenticado) {
        if (autenticado.isPortalAluno()) {
            return TipoAcesso.ALUNO;
        }
        return TipoAcesso.COLABORADOR;
    }

    public List<String> obterPermissoes(UsuarioAutenticado autenticado) {
        if (autenticado.isPortalAluno()) {
            return EnumSet.allOf(PermissaoSistema.class).stream()
                    .filter(p -> p.getCodigo().startsWith("aluno-portal:"))
                    .map(PermissaoSistema::getCodigo)
                    .toList();
        }
        Funcionario funcionario = autenticado.getFuncionario();
        if (funcionario == null || funcionario.getTipoFuncionario() == null) {
            return List.of();
        }
        return new ArrayList<>(TipoFuncionario.codigosPermissao(
                funcionario.getTipoFuncionario(),
                funcionario.getAreaTerceirizado()));
    }

    public SituacaoCobranca resolverSituacaoCobranca(String vinculo, UsuarioAutenticado autenticado) {
        TipoAcesso tipoAcesso = resolverTipoAcesso(autenticado);
        if (tipoAcesso == TipoAcesso.ALUNO) {
            return servicoSituacaoCobranca.situacaoMensalidadeAluno(autenticado.getAluno());
        }
        if (autenticado.getFuncionario() != null && autenticado.getFuncionario().isUsuarioMaster()) {
            return SituacaoCobranca.ATIVO;
        }
        try {
            return servicoSituacaoCobranca.situacaoPlanoInstituicao(Long.parseLong(vinculo));
        } catch (NumberFormatException e) {
            return SituacaoCobranca.BLOQUEADO;
        }
    }

    public boolean planoAtivoParaVinculo(String vinculo, TipoAcesso tipoAcesso, UsuarioAutenticado autenticado) {
        return resolverSituacaoCobranca(vinculo, autenticado).permiteAcesso();
    }

    /** Mantido por compatibilidade com chamadas antigas. */
    public List<String> obterPermissoes(Funcionario funcionario) {
        if (funcionario == null || funcionario.getTipoFuncionario() == null) {
            return List.of();
        }
        return new ArrayList<>(TipoFuncionario.codigosPermissao(
                funcionario.getTipoFuncionario(),
                funcionario.getAreaTerceirizado()));
    }
}
