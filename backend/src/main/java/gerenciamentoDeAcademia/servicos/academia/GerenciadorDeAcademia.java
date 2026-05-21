package gerenciamentoDeAcademia.servicos.academia;

import gerenciamentoDeAcademia.dto.AcademiaDto;
import gerenciamentoDeAcademia.dto.AtivacaoFuncionarioDto;
import gerenciamentoDeAcademia.enums.AreaTerceirizado;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.entidades.Academia;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.UserRole;
import gerenciamentoDeAcademia.excecao.ApplicationException;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AcademiaRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.UsuarioRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IGerenciadorDeAcademia;
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
public class GerenciadorDeAcademia implements IGerenciadorDeAcademia {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final AcademiaRepository academiaRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public void cadastrar(AcademiaDto academiaDto) {
        if (academiaRepository.findByCnpj(academiaDto.getCnpj()) != null) {
            throw new ApplicationException("Academia já cadastrada!", HttpStatus.BAD_REQUEST);
        }

        academiaRepository.save(new Academia(academiaDto));
    }

    @Override
    public void desativarAcademia(String cnpjAcademia) {
        Academia academiaParaDesativar = academiaRepository.findByCnpj(cnpjAcademia);
        ExcecaoDeDominio.quandoNulo(academiaParaDesativar, "Academia não encontrada para desativar!");

        if (!academiaParaDesativar.getCadastroAtivo()) {
            throw new ApplicationException("Essa academia já está desativada!", HttpStatus.BAD_REQUEST);
        }

        academiaParaDesativar.setCadastroAtivo(false);
        academiaRepository.save(academiaParaDesativar);
    }

    @Override
    @Transactional
    public void atualizarDados(AcademiaDto academiaDto) {
        Academia academia = Optional.ofNullable(academiaRepository.findByCnpj(academiaDto.getCnpj()))
                .orElseThrow(() -> new ExcecaoDeDominio("Academia não encontrada"));

        academia.atualizarCadastro(academiaDto);
    }

    @Override
    public AcademiaDto consultarAcademiaCnpj(String cnpjAcademia) {
        Academia academia = academiaRepository.findByCnpj(cnpjAcademia);
        ExcecaoDeDominio.quandoNulo(academia, "Academia não encontrada");

        return new AcademiaDto(academia);
    }

    @Override
    public List<AcademiaDto> consultarTodasAcademias() {
        return academiaRepository.findAll()
                .stream()
                .map(AcademiaDto::new)
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
        Academia academia = academiaRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada"));
        Funcionario funcionario = buscarFuncionario(cpf);
        aplicarFuncaoNaInstituicao(funcionario, dados);
        if (!academia.getFuncionarios().contains(funcionario)) {
            academia.getFuncionarios().add(funcionario);
        }
        ativarFuncionarioInterno(academia, funcionario);
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

    private void ativarFuncionarioInterno(Academia academia, Funcionario funcionario) {
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

        academia.atualizarStatusPendencias();
        academiaRepository.save(academia);
    }

    @Override
    @Transactional
    public void ativarFuncionario(String cpf, String cnpj) {
        Academia academia = buscarAcademia(cnpj);
        Funcionario funcionario = buscarFuncionario(cpf);
        if (!academia.getFuncionarios().contains(funcionario)) {
            academia.getFuncionarios().add(funcionario);
        }
        ativarFuncionarioInterno(academia, funcionario);
    }

    @Override
    public void inativarFuncionario(String cpf, String cnpj) {
        inativarFuncionarioNaInstituicao(buscarAcademia(cnpj).getId(), cpf);
    }

    @Override
    @Transactional
    public void inativarFuncionarioNaInstituicao(Long instituicaoId, String cpf) {
        Academia academia = academiaRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada"));
        Funcionario funcionario = buscarFuncionario(cpf);
        academia.validarVinculo(funcionario);
        funcionario.inativar();
        funcionarioRepository.save(funcionario);
        academia.atualizarStatusPendencias();
        academiaRepository.save(academia);
    }

    @Override
    public boolean verificarVinculo(String cpf, String vinculo) {
        return academiaRepository.existsByCnpjAndFuncionarioCpf(Long.parseLong(vinculo), cpf);
    }

    @Override
    public AcademiaDto consultarAcademiaId(Long codAcademia) {
        return academiaRepository.findById(codAcademia)
                .map(AcademiaDto::new)
                .orElseThrow(() -> new ApplicationException("Academia não encontrada", HttpStatus.NOT_FOUND));
    }

    private Academia buscarAcademia(String cnpj) {
        return Optional.ofNullable(academiaRepository.findByCnpj(cnpj))
                .orElseThrow(() -> new ApplicationException("Academia não encontrada", HttpStatus.NOT_FOUND));
    }

    private Funcionario buscarFuncionario(String cpf) {
        return Optional.ofNullable(funcionarioRepository.findByCpf(cpf))
                .orElseThrow(() -> new ApplicationException("Funcionário não encontrado", HttpStatus.NOT_FOUND));
    }
}
