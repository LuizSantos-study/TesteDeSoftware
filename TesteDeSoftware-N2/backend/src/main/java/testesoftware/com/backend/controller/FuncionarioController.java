package testesoftware.com.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import testesoftware.com.backend.dto.CadastroFuncionarioRequest;
import testesoftware.com.backend.dto.FuncionarioResponse;
import testesoftware.com.backend.dto.LoginRequest;
import testesoftware.com.backend.model.Funcionario;
import testesoftware.com.backend.service.FuncionarioService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para Funcionários: consulta, cadastro (tela de Cadastro) e login (tela de Login).
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
    public ResponseEntity<List<FuncionarioResponse>> listarTodos() {
        return ResponseEntity.ok(funcionarioService.listarTodos().stream().map(FuncionarioResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(FuncionarioResponse.from(funcionarioService.buscarPorId(id)));
    }

    @GetMapping("/farmaceuticos")
    public ResponseEntity<List<FuncionarioResponse>> listarFarmaceuticos() {
        return ResponseEntity.ok(funcionarioService.listarFarmaceuticos().stream().map(FuncionarioResponse::from).toList());
    }

    @PostMapping({"", "/cadastrar"})
    public ResponseEntity<FuncionarioResponse> cadastrar(@Valid @RequestBody CadastroFuncionarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(FuncionarioResponse.from(funcionarioService.cadastrar(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Funcionario autenticado = funcionarioService.autenticar(request.email(), request.senha());
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("autenticado", true);
        resposta.put("mensagem", "Login realizado com sucesso.");
        resposta.put("funcionario", FuncionarioResponse.from(autenticado));
        return ResponseEntity.ok(resposta);
    }
}
