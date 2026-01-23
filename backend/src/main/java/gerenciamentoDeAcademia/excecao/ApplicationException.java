package gerenciamentoDeAcademia.excecao;

import org.springframework.http.HttpStatus;

public class ApplicationException extends RuntimeException {
    private final HttpStatus status;

    public ApplicationException(String mensagem, HttpStatus status) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
