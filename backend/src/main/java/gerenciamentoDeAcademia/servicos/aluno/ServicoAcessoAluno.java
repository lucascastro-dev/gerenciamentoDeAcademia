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
        if (usuarioRepository.existsByLogin(cpf)) {
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .login(cpf)
                .password(passwordEncoder.encode(senhaInicialAluno))
                .role(UserRole.ALUNO)
                .build());
    }
}
