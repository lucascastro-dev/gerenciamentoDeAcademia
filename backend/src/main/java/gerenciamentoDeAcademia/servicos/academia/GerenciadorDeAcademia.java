package gerenciamentoDeAcademia.servicos.academia;

import gerenciamentoDeAcademia.dto.AcademiaDto;
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
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GerenciadorDeAcademia implements IGerenciadorDeAcademia {

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
    public void atualizarDados(AcademiaDto academiaDto) {
        Academia academia = Optional.ofNullable(academiaRepository.findByCnpj(academiaDto.getCnpj()))
                .orElseThrow(() -> new ExcecaoDeDominio("Academia não encontrada"));

        if (academia.getFuncionarios() != null && !academia.getFuncionarios().isEmpty()) {
            academia.getFuncionarios().forEach(funcionario -> {
                Funcionario funcionarioEncontrado = funcionarioRepository.findByCpf(funcionario.getCpf());
                ExcecaoDeDominio.quandoNulo(funcionarioEncontrado,
                        "Funcionário não encontrado para vincular à academia, faça o registro do funcionário!");
            });
        }

        academiaRepository.save(academia);
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
    public void solicitarPrimeiroAcesso(String cpf, String cnpj) {
        Academia academia = academiaRepository.findByCnpj(cnpj);
        ExcecaoDeDominio.quandoNulo(academia, "Academia não encontrada");
        Funcionario funcionario = funcionarioRepository.findByCpf(cpf);
        ExcecaoDeDominio.quandoNulo(funcionario, "Funcionário não encontrado");
        academia.setPossuiCadastrosParaAprovar(true);
        academia.getFuncionarios().add(funcionario);
        academiaRepository.save(academia);
    }

    @Override
    @Transactional
    public void ativarFuncionario(String cpf, String cnpj) {
        Academia academia = buscarAcademia(cnpj);
        Funcionario funcionario = buscarFuncionario(cpf);

        academia.validarVinculo(funcionario);
        funcionario.ativar();
        funcionarioRepository.save(funcionario);
        usuarioRepository.save(new Usuario(funcionario.getCpf(), funcionario.getSenha(), UserRole.USER));

        academia.atualizarStatusPendencias();
        academiaRepository.save(academia);
    }

    @Override
    public void inativarFuncionario(String cpf, String cnpj) {
        Academia academia = buscarAcademia(cnpj);
        Funcionario funcionario = buscarFuncionario(cpf);

        academia.validarVinculo(funcionario);
        funcionario.inativar();
        funcionarioRepository.save(funcionario);
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
