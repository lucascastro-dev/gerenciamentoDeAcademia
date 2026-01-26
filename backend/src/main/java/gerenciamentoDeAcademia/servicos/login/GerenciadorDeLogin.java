package gerenciamentoDeAcademia.servicos.login;

import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.UsuarioRepository;
import gerenciamentoDeAcademia.servicos.academia.GerenciadorDeAcademia;
import gerenciamentoDeAcademia.servicos.funcionario.ConsultaDeFuncionario;
import gerenciamentoDeAcademia.servicos.interfaces.IGerenciadorDeLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GerenciadorDeLogin implements IGerenciadorDeLogin, UserDetailsService {

    @Autowired
    private ConsultaDeFuncionario consultaDeFuncionario;

    @Autowired
    private GerenciadorDeAcademia gerenciadorDeAcademia;

    @Autowired
    private UsuarioRepository repository;

    @Override
    public void validarLogin(String login, String vinculo) {
        Funcionario funcionario = consultaDeFuncionario.consultarFuncionarioPorCpf(login);
        ExcecaoDeDominio.quandoNulo(funcionario, "Usuário ou senha inválidos");
        ExcecaoDeDominio.quando(!funcionario.getCadastroAtivo(), "Seu cadastro não está ativo, entre em contato com a adminitração da academia!");

        boolean ehVinculado = gerenciadorDeAcademia.verificarVinculo(funcionario.getCpf(), vinculo);

        if (!ehVinculado) {
            throw new ExcecaoDeDominio("Usuário não possui vínculo com a academia informada.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = repository.findByLogin(username);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
        return usuario;
    }
}
