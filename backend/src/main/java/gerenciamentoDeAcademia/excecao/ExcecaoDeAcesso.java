package gerenciamentoDeAcademia.excecao;

import org.springframework.http.HttpStatus;

public class ExcecaoDeAcesso extends ApplicationException {

    public ExcecaoDeAcesso(String mensagem, HttpStatus status) {
        super(mensagem, status);
    }

    public static void acessoNegado(String mensagem) {
        throw new ExcecaoDeAcesso(mensagem, HttpStatus.FORBIDDEN);
    }

    public static void naoEncontrado(String mensagem) {
        throw new ExcecaoDeAcesso(mensagem, HttpStatus.NOT_FOUND);
    }
}
