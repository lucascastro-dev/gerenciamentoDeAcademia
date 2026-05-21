package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.dto.AlterarSenhaDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlteradorSenha {

    private final UsuarioRepository usuarioRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void alterarSenhaDoUsuarioLogado(String cpf, AlterarSenhaDto dto) {
        ExcecaoDeDominio.quandoNulo(dto, "Dados de senha são obrigatórios");
        ExcecaoDeDominio.quandoNuloOuVazio(dto.getSenhaAtual(), "Informe a senha atual");
        ExcecaoDeDominio.quandoNuloOuVazio(dto.getSenhaNova(), "Informe a nova senha");
        ExcecaoDeDominio.quando(dto.getSenhaNova().length() < 8, "A nova senha deve ter no mínimo 8 caracteres");

        Usuario usuario = usuarioRepository.findByLogin(cpf);
        ExcecaoDeDominio.quandoNulo(usuario, "Usuário não encontrado");

        if (!passwordEncoder.matches(dto.getSenhaAtual(), usuario.getPassword())) {
            throw new ExcecaoDeDominio("Senha atual incorreta");
        }

        usuario.setPassword(passwordEncoder.encode(dto.getSenhaNova()));
        usuarioRepository.save(usuario);

        Funcionario funcionario = funcionarioRepository.findByCpf(cpf);
        if (funcionario != null) {
            funcionario.setSenha(dto.getSenhaNova());
            funcionarioRepository.save(funcionario);
        }
    }
}
