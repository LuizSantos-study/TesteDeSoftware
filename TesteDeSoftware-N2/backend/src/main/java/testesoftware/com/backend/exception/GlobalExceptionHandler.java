package testesoftware.com.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tratador global de exceções da API REST para padronizar as respostas de erro em JSON.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            DescontoInvalidoException.class,
            MedicamentoControladoException.class,
            VendaFinalizadaException.class
    })
    public ResponseEntity<Map<String, Object>> tratarExcecoesDominio(RuntimeException ex) {
        return construirRespostaErro(HttpStatus.BAD_REQUEST, ex.getClass().getSimpleName(), ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarIllegalArgument(IllegalArgumentException ex) {
        String mensagem = ex.getMessage() != null ? ex.getMessage() : "Requisição inválida.";
        if (mensagem.toLowerCase().contains("credenciais inválidas")) {
            return construirRespostaErro(HttpStatus.UNAUTHORIZED, "NaoAutorizado", mensagem);
        }
        if (mensagem.toLowerCase().contains("não encontrado") || mensagem.toLowerCase().contains("não encontrada")) {
            return construirRespostaErro(HttpStatus.NOT_FOUND, "RecursoNaoEncontrado", mensagem);
        }
        return construirRespostaErro(HttpStatus.BAD_REQUEST, "DadosInvalidos", mensagem);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> tratarIllegalState(IllegalStateException ex) {
        String mensagem = ex.getMessage() != null ? ex.getMessage() : "Operação não permitida.";
        if (mensagem.toLowerCase().contains("inativo")) {
            return construirRespostaErro(HttpStatus.FORBIDDEN, "UsuarioInativo", mensagem);
        }
        return construirRespostaErro(HttpStatus.BAD_REQUEST, "EstadoInvalido", mensagem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(MethodArgumentNotValidException ex) {
        String erros = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return construirRespostaErro(HttpStatus.BAD_REQUEST, "ErroValidacao", erros);
    }

    private ResponseEntity<Map<String, Object>> construirRespostaErro(HttpStatus status, String erro, String mensagem) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("erro", erro);
        body.put("mensagem", mensagem);
        return ResponseEntity.status(status).body(body);
    }
}
