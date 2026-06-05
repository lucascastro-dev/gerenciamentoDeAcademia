package gerenciamentoDeAcademia.servicos.login;



import gerenciamentoDeAcademia.dto.VinculoInstituicaoDto;

import gerenciamentoDeAcademia.entidades.Aluno;

import gerenciamentoDeAcademia.entidades.Funcionario;

import gerenciamentoDeAcademia.entidades.Instituicao;

import gerenciamentoDeAcademia.entidades.Usuario;

import gerenciamentoDeAcademia.enums.PermissaoSistema;

import gerenciamentoDeAcademia.enums.SituacaoCobranca;

import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;

import gerenciamentoDeAcademia.enums.TipoAcesso;

import gerenciamentoDeAcademia.enums.TipoFuncionario;

import gerenciamentoDeAcademia.enums.UserRole;

import gerenciamentoDeAcademia.excecao.ApplicationException;

import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;

import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;

import gerenciamentoDeAcademia.infra.seguranca.VinculoPlataforma;

import gerenciamentoDeAcademia.repositorios.AlunoRepository;

import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;

import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;

import gerenciamentoDeAcademia.repositorios.UsuarioRepository;

import gerenciamentoDeAcademia.servicos.instituicao.GerenciadorDeInstituicao;

import gerenciamentoDeAcademia.servicos.funcionario.ConsultaDeFuncionario;

import gerenciamentoDeAcademia.servicos.interfaces.IGerenciadorDeLogin;

import gerenciamentoDeAcademia.servicos.cobranca.ServicoSituacaoCobranca;

import gerenciamentoDeAcademia.servicos.master.ServicoMasterPlataforma;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;



import java.util.ArrayList;

import java.util.Arrays;

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



    @Autowired

    private ServicoMasterPlataforma servicoMasterPlataforma;



    public List<VinculoInstituicaoDto> listarInstituicoesPorCpf(String cpf) {

        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "Informe o CPF");

        Funcionario funcionario = funcionarioRepository.findByCpf(cpf);

        List<VinculoInstituicaoDto> vinculos = new ArrayList<>();

        if (funcionario != null && servicoMasterPlataforma.ehOperadorPlataforma(funcionario)) {
            vinculos.add(VinculoInstituicaoDto.plataforma());
        }

        if (funcionario != null) {
            vinculos.addAll(instituicaoRepository.findInstituicoesPorCpfFuncionario(cpf).stream()
                    .map(VinculoInstituicaoDto::daInstituicao)
                    .filter(VinculoInstituicaoDto::selecionavel)
                    .toList());
            return vinculos;
        }

        Aluno aluno = alunoRepository.findByCpf(cpf);

        if (aluno != null) {
            return instituicaoRepository.findInstituicoesPorCpfAluno(cpf).stream()
                    .map(VinculoInstituicaoDto::daInstituicao)
                    .filter(VinculoInstituicaoDto::selecionavel)
                    .toList();
        }

        return vinculos;

    }



    public String solicitarRecuperacaoSenha(String cpf) {

        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "Informe o CPF");

        Funcionario funcionario = funcionarioRepository.findByCpf(cpf);

        Aluno aluno = alunoRepository.findByCpf(cpf);

        ExcecaoDeDominio.quando(funcionario == null && aluno == null, "CPF não encontrado no sistema");

        if (funcionario != null && servicoMasterPlataforma.ehOperadorPlataforma(funcionario)) {

            return "Solicitação registrada. O envio de e-mail para redefinição de senha será implementado em breve.";

        }

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

            validarCobrancaAluno(login, vinculo);

            return;

        }



        Funcionario funcionario = consultaDeFuncionario.consultarFuncionarioPorCpf(login);

        ExcecaoDeDominio.quandoNulo(funcionario, "Usuário ou senha inválidos.");

        ExcecaoDeDominio.quando(!funcionario.getCadastroAtivo(),

                "Seu cadastro não está ativo. Entre em contato com a administração da instituição.");



        if (servicoMasterPlataforma.ehOperadorPlataforma(funcionario) && VinculoPlataforma.ehVinculoPlataforma(vinculo)) {
            return;
        }

        validarInstituicaoAtivaParaLogin(vinculo);

        boolean ehVinculado = gerenciadorDeInstituicao.verificarVinculo(funcionario.getCpf(), vinculo);

        if (!ehVinculado) {

            throw new ExcecaoDeDominio("Usuário não possui vínculo com a instituição informada.");

        }

        if (!servicoMasterPlataforma.ehOperadorPlataforma(funcionario)) {

            validarCobrancaInstituicao(vinculo);

        }

    }



    private void validarCobrancaAluno(String cpf, String vinculo) {

        Aluno aluno = alunoRepository.findByCpf(cpf);

        Long instituicaoId = parseInstituicaoId(vinculo);

        SituacaoCobranca situacao = servicoSituacaoCobranca.situacaoMensalidadeAluno(aluno, instituicaoId);

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



    private void validarInstituicaoAtivaParaLogin(String vinculo) {
        if (VinculoPlataforma.ehVinculoPlataforma(vinculo)) {
            return;
        }
        try {
            Long id = Long.parseLong(vinculo);
            var instituicao = instituicaoRepository.findById(id)
                    .orElseThrow(() -> new ExcecaoDeDominio("Instituição inválida."));
            ExcecaoDeDominio.quando(!Boolean.TRUE.equals(instituicao.getCadastroAtivo()),
                    "A instituição selecionada está inativa. Entre em contato com o suporte da plataforma.");
        } catch (NumberFormatException e) {
            throw new ExcecaoDeDominio("Instituição inválida.");
        }
    }

    private void validarLoginAluno(String cpf, String vinculo) {

        Aluno aluno = alunoRepository.findByCpf(cpf);

        ExcecaoDeDominio.quandoNulo(aluno, "Usuário ou senha inválidos.");

        validarInstituicaoAtivaParaLogin(vinculo);

        try {

            Long id = Long.parseLong(vinculo);

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



    public UsuarioAutenticado montarSessaoAutenticada(UsuarioAutenticado base, String vinculo) {

        if (base.isPortalAluno()) {

            Long instituicaoId = parseInstituicaoId(vinculo);

            SituacaoCobranca situacao = servicoSituacaoCobranca.situacaoMensalidadeAluno(base.getAluno(), instituicaoId);

            return new UsuarioAutenticado(

                    base.getUsuario(), null, base.getAluno(), instituicaoId, situacao,

                    StatusFinanceiroInstituicao.NAO_APLICAVEL, false, false);

        }

        Funcionario funcionario = base.getFuncionario();

        boolean operador = servicoMasterPlataforma.ehOperadorPlataforma(funcionario);

        boolean masterRaiz = funcionario != null && servicoMasterPlataforma.ehMasterRaiz(funcionario.getCpf());

        if (operador && VinculoPlataforma.ehVinculoPlataforma(vinculo)) {

            return new UsuarioAutenticado(

                    base.getUsuario(), funcionario, null, VinculoPlataforma.ID_LONG,

                    SituacaoCobranca.ATIVO, StatusFinanceiroInstituicao.NAO_APLICAVEL, true, masterRaiz);

        }

        Long instituicaoId = parseInstituicaoId(vinculo);

        StatusFinanceiroInstituicao statusFinanceiro = resolverStatusFinanceiro(instituicaoId);

        SituacaoCobranca situacao = operador

                ? SituacaoCobranca.ATIVO

                : resolverSituacaoCobrancaVinculo(vinculo);

        return new UsuarioAutenticado(

                base.getUsuario(), funcionario, null, instituicaoId, situacao,

                statusFinanceiro, operador, masterRaiz);

    }



    private StatusFinanceiroInstituicao resolverStatusFinanceiro(Long instituicaoId) {

        if (instituicaoId == null || instituicaoId <= 0) {

            return StatusFinanceiroInstituicao.NAO_APLICAVEL;

        }

        return instituicaoRepository.findById(instituicaoId)

                .map(Instituicao::getStatusFinanceiro)

                .map(s -> s != null ? s : StatusFinanceiroInstituicao.NAO_APLICAVEL)

                .orElse(StatusFinanceiroInstituicao.NAO_APLICAVEL);

    }



    private Long parseInstituicaoId(String vinculo) {

        if (vinculo == null || vinculo.isBlank() || VinculoPlataforma.ehVinculoPlataforma(vinculo)) {

            return VinculoPlataforma.ID_LONG;

        }

        try {

            return Long.parseLong(vinculo);

        } catch (NumberFormatException e) {

            return null;

        }

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

        if (autenticado.isOperadorPlataforma()) {

            return Arrays.stream(PermissaoSistema.values())

                    .map(PermissaoSistema::getCodigo)

                    .filter(c -> !c.startsWith("aluno-portal:"))

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

        if (autenticado.isPortalAluno()) {

            Long instituicaoId = parseInstituicaoId(vinculo);

            return servicoSituacaoCobranca.situacaoMensalidadeAluno(autenticado.getAluno(), instituicaoId);

        }

        if (autenticado.isOperadorPlataforma()) {

            return SituacaoCobranca.ATIVO;

        }

        return resolverSituacaoCobrancaVinculo(vinculo);

    }



    private SituacaoCobranca resolverSituacaoCobrancaVinculo(String vinculo) {

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


