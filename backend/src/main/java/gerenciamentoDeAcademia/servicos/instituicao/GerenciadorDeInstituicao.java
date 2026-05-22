package gerenciamentoDeAcademia.servicos.instituicao;

import gerenciamentoDeAcademia.dto.AtivacaoFuncionarioDto;
import gerenciamentoDeAcademia.dto.InstituicaoDto;
import gerenciamentoDeAcademia.enums.AreaTerceirizado;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.UserRole;
import gerenciamentoDeAcademia.excecao.ApplicationException;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.UsuarioRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IGerenciadorDeInstituicao;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GerenciadorDeInstituicao implements IGerenciadorDeInstituicao {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final InstituicaoRepository instituicaoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public void cadastrar(InstituicaoDto instituicaoDto) {
        if (instituicaoRepository.findByCnpj(instituicaoDto.getCnpj()) != null) {
            throw new ApplicationException("Instituição já cadastrada!", HttpStatus.BAD_REQUEST);
        }

        instituicaoRepository.save(new Instituicao(instituicaoDto));
    }

    @Override
    public void desativarInstituicao(String cnpjInstituicao) {
        Instituicao instituicaoParaDesativar = instituicaoRepository.findByCnpj(cnpjInstituicao);
        ExcecaoDeDominio.quandoNulo(instituicaoParaDesativar, "Instituição não encontrada para desativar!");

        if (!instituicaoParaDesativar.getCadastroAtivo()) {
            throw new ApplicationException("Essa instituição já está desativada!", HttpStatus.BAD_REQUEST);
        }

        instituicaoParaDesativar.setCadastroAtivo(false);
        instituicaoRepository.save(instituicaoParaDesativar);
    }

    @Override
    @Transactional
    public void atualizarDados(InstituicaoDto instituicaoDto) {
        Instituicao instituicao = Optional.ofNullable(instituicaoRepository.findByCnpj(instituicaoDto.getCnpj()))
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada"));

        instituicao.atualizarCadastro(instituicaoDto);
    }

    @Override
    public InstituicaoDto consultarInstituicaoCnpj(String cnpjInstituicao) {
        Instituicao instituicao = instituicaoRepository.findByCnpj(cnpjInstituicao);
        ExcecaoDeDominio.quandoNulo(instituicao, "Instituição não encontrada");

        return new InstituicaoDto(instituicao);
    }

    @Override
    public List<InstituicaoDto> consultarTodasInstituicoes() {
        return instituicaoRepository.findAll()
                .stream()
                .map(InstituicaoDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public void solicitarPrimeiroAcesso(String cpf) {
        Funcionario funcionario = funcionarioRepository.findByCpf(cpf);
        ExcecaoDeDominio.quandoNulo(funcionario, "CPF não encontrado. Faça o pré-cadastro ou fale com o RH.");
        ExcecaoDeDominio.quando(Boolean.TRUE.equals(funcionario.getCadastroAtivo()),
                "Seu cadastro já está ativo. Use a tela de login.");
    }

    @Override
    @Transactional
    public void ativarFuncionarioNaInstituicao(Long instituicaoId, String cpf, AtivacaoFuncionarioDto dados) {
        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada"));
        Funcionario funcionario = buscarFuncionario(cpf);
        aplicarFuncaoNaInstituicao(funcionario, dados);
        if (!instituicao.getFuncionarios().contains(funcionario)) {
            instituicao.getFuncionarios().add(funcionario);
        }
        ativarFuncionarioInterno(instituicao, funcionario);
    }

    private void aplicarFuncaoNaInstituicao(Funcionario funcionario, AtivacaoFuncionarioDto dados) {
        ExcecaoDeDominio.quandoNulo(dados, "Informe a função do colaborador na instituição.");
        ExcecaoDeDominio.quandoNulo(dados.getTipoFuncionario(), "Tipo de funcionário é obrigatório na ativação.");
        if (dados.getTipoFuncionario() == TipoFuncionario.DIRETOR) {
            throw new ExcecaoDeDominio("Perfil Diretor não pode ser atribuído por esta tela.");
        }
        if (dados.getTipoFuncionario() == TipoFuncionario.TERCEIRIZADO) {
            ExcecaoDeDominio.quandoNulo(dados.getAreaTerceirizado(),
                    "Informe a área do terceirizado (RH, professor substituto ou TI).");
            funcionario.setAreaTerceirizado(dados.getAreaTerceirizado());
        } else {
            funcionario.setAreaTerceirizado(null);
        }
        if (dados.getTipoFuncionario() == TipoFuncionario.PROFESSOR) {
            ExcecaoDeDominio.quandoNuloOuVazio(dados.getEspecializacao(),
                    "Especialização é obrigatória para professores.");
            funcionario.setEspecializacao(dados.getEspecializacao());
        }
        funcionario.setTipoFuncionario(dados.getTipoFuncionario());
        funcionario.setCargo(dados.getTipoFuncionario().getDescricao());
        funcionarioRepository.save(funcionario);
    }

    private void ativarFuncionarioInterno(Instituicao instituicao, Funcionario funcionario) {
        String cpf = funcionario.getCpf();
        if (usuarioRepository.existsByLogin(cpf) && Boolean.TRUE.equals(funcionario.getCadastroAtivo())) {
            throw new ExcecaoDeDominio("Este colaborador já possui cadastro ativo.");
        }
        funcionario.ativar();
        funcionarioRepository.save(funcionario);

        if (!usuarioRepository.existsByLogin(cpf)) {
            String senhaCriptografada = passwordEncoder.encode(funcionario.getSenha());
            UserRole role = funcionario.isUsuarioMaster() ? UserRole.ADMIN : UserRole.USER;
            usuarioRepository.save(Usuario.builder()
                    .login(cpf)
                    .password(senhaCriptografada)
                    .role(role)
                    .build());
        }

        instituicao.atualizarStatusPendencias();
        instituicaoRepository.save(instituicao);
    }

    @Override
    @Transactional
    public void ativarFuncionario(String cpf, String cnpj) {
        Instituicao instituicao = buscarInstituicao(cnpj);
        Funcionario funcionario = buscarFuncionario(cpf);
        if (!instituicao.getFuncionarios().contains(funcionario)) {
            instituicao.getFuncionarios().add(funcionario);
        }
        ativarFuncionarioInterno(instituicao, funcionario);
    }

    @Override
    public void inativarFuncionario(String cpf, String cnpj) {
        inativarFuncionarioNaInstituicao(buscarInstituicao(cnpj).getId(), cpf);
    }

    @Override
    @Transactional
    public void inativarFuncionarioNaInstituicao(Long instituicaoId, String cpf) {
        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada"));
        Funcionario funcionario = buscarFuncionario(cpf);
        instituicao.validarVinculo(funcionario);
        funcionario.inativar();
        funcionarioRepository.save(funcionario);
        instituicao.atualizarStatusPendencias();
        instituicaoRepository.save(instituicao);
    }

    @Override
    public boolean verificarVinculo(String cpf, String vinculo) {
        return instituicaoRepository.existsByCnpjAndFuncionarioCpf(Long.parseLong(vinculo), cpf);
    }

    @Override
    public InstituicaoDto consultarInstituicaoId(Long codInstituicao) {
        return instituicaoRepository.findById(codInstituicao)
                .map(InstituicaoDto::new)
                .orElseThrow(() -> new ApplicationException("Instituição não encontrada", HttpStatus.NOT_FOUND));
    }

    private Instituicao buscarInstituicao(String cnpj) {
        return Optional.ofNullable(instituicaoRepository.findByCnpj(cnpj))
                .orElseThrow(() -> new ApplicationException("Instituição não encontrada", HttpStatus.NOT_FOUND));
    }

    private Funcionario buscarFuncionario(String cpf) {
        return Optional.ofNullable(funcionarioRepository.findByCpf(cpf))
                .orElseThrow(() -> new ApplicationException("Funcionário não encontrado", HttpStatus.NOT_FOUND));
    }
}
