package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.UserRole;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicoAcessoAluno {

    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.aluno.senha-inicial:123}")
    private String senhaInicialAluno;

    @Transactional
    public void garantirUsuarioPortal(Aluno aluno) {
        ExcecaoDeDominio.quandoNulo(aluno, "Aluno inválido.");
        String cpf = aluno.getCpf();
        String senha = senhaInicialDoCpf(cpf);
        if (usuarioRepository.existsByLogin(cpf)) {
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .login(cpf)
                .password(passwordEncoder.encode(senha))
                .role(UserRole.ALUNO)
                .build());
    }

    /** Senha inicial do portal: 6 primeiros dígitos do CPF (apenas números). */
    public static String senhaInicialDoCpf(String cpf) {
        String digitos = cpf != null ? cpf.replaceAll("\\D", "") : "";
        if (digitos.length() >= 6) {
            return digitos.substring(0, 6);
        }
        return digitos.isEmpty() ? "123456" : digitos;
    }
}
