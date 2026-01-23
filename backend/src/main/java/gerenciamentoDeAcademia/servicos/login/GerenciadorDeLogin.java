package gerenciamentoDeAcademia.servicos.login;

import gerenciamentoDeAcademia.dto.AcademiaDto;
import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.servicos.academia.GerenciadorDeAcademia;
import gerenciamentoDeAcademia.servicos.funcionario.ConsultaDeFuncionario;
import gerenciamentoDeAcademia.servicos.interfaces.IGerenciadorDeLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GerenciadorDeLogin implements IGerenciadorDeLogin {

    @Autowired
    private ConsultaDeFuncionario consultaDeFuncionario;

    @Autowired
    private GerenciadorDeAcademia gerenciadorDeAcademia;

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
}
