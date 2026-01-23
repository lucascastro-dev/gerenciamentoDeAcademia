package gerenciamentoDeAcademia.excecao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GerenciadorDeExcecoes {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<String> tratarApplicationException(ApplicationException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }
}