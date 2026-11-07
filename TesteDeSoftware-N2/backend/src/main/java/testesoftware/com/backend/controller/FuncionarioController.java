package testesoftware.com.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import testesoftware.com.backend.model.Funcionario;
import testesoftware.com.backend.service.FuncionarioService;

import java.util.List;

/**
 * Controller REST para consulta de Funcionários no PDV (somente leitura - dados pré-cadastrados no banco).
 */
@RestController
@RequestMapping("/api/funcionarios")
@CrossOrigin(origins = "*")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    public ResponseEntity<List<Funcionario>> listarTodos() {
        return ResponseEntity.ok(funcionarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Funcionario> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(funcionarioService.buscarPorId(id));
    }

    @GetMapping("/farmaceuticos")
    public ResponseEntity<List<Funcionario>> listarFarmaceuticos() {
        return ResponseEntity.ok(funcionarioService.listarFarmaceuticos());
    }
}
