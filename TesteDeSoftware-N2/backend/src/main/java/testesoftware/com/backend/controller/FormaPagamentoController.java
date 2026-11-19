package testesoftware.com.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import testesoftware.com.backend.model.FormaPagamento;
import testesoftware.com.backend.service.FormaPagamentoService;

import java.util.List;

/**
 * Controller REST para Formas de Pagamento (GET, POST, PUT, DELETE).
 */
@RestController
@RequestMapping("/api/formas-pagamento")
@CrossOrigin(origins = "*")
public class FormaPagamentoController {

    private final FormaPagamentoService formaPagamentoService;

    public FormaPagamentoController(FormaPagamentoService formaPagamentoService) {
        this.formaPagamentoService = formaPagamentoService;
    }

    @GetMapping
    public ResponseEntity<List<FormaPagamento>> listarTodas() {
        return ResponseEntity.ok(formaPagamentoService.listarTodas());
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<FormaPagamento>> listarAtivas() {
        return ResponseEntity.ok(formaPagamentoService.listarAtivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormaPagamento> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(formaPagamentoService.buscarPorId(id));
    }

}