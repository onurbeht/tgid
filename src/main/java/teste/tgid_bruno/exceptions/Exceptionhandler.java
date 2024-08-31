package teste.tgid_bruno.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class Exceptionhandler {

    @ExceptionHandler(SaldoException.class)
    public ResponseEntity<?> handleSaldoException() {
        return ResponseEntity.status(HttpStatusCode.valueOf(403))
                .body("Saldo insuficiente para saque! verifique o valor e tente novamente.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleCreateUserException(MethodArgumentNotValidException msg) {
        String rawMsg = msg.getMessage().substring(msg.getMessage().lastIndexOf("default message"));

        // default message [não deve estar em branco]] ou default message [deve ser
        // maior que 0]]
        String responseMsg = rawMsg.subSequence(17, rawMsg.length() - 3).toString();

        return ResponseEntity.badRequest().body(responseMsg);
    }
}
