package gerenciamentoDeAcademia.excecao;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GerenciadorDeExcecoes {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<String> tratarApplicationException(ApplicationException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> tratarIntegridade(DataIntegrityViolationException ex) {
        return ResponseEntity.badRequest()
                .body("Não foi possível salvar os dados. Verifique vínculos, campos obrigatórios e tente novamente.");
    }
}