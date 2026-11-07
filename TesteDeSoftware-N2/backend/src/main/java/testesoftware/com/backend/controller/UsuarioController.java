package testesoftware.com.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import testesoftware.com.backend.model.Usuario;
import testesoftware.com.backend.service.UsuarioService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para autenticação simples (Login / Criar Conta) e CRUD completo de Usuários.
 */
@RestController
@RequestMapping({"/api/usuarios"})
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * GET /api/usuarios - Lista todos os usuários cadastrados.
     */
    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    /**
     * GET /api/usuarios/{id} - Busca um usuário pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    /**
     * POST /api/usuarios (ou /api/usuarios/registrar)
     * Permite criar uma nova conta de usuário caso ainda não tenha conta.
     */
    @PostMapping({"", "/registrar"})
    public ResponseEntity<Usuario> criarConta(@RequestBody Usuario usuario) {
        Usuario criado = usuarioService.criar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * POST /api/usuarios/login
     * Realiza a autenticação simples do usuário com login (ou email) e senha.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credenciais) {
        String loginOuEmail = credenciais.get("login");
        if (loginOuEmail == null || loginOuEmail.isBlank()) {
            loginOuEmail = credenciais.get("email");
        }
        String senha = credenciais.get("senha");

        Usuario autenticado = usuarioService.autenticar(loginOuEmail, senha);

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("autenticado", true);
        resposta.put("mensagem", "Login realizado com sucesso.");
        resposta.put("usuario", autenticado);
        return ResponseEntity.ok(resposta);
    }

    /**
     * PUT /api/usuarios/{id} - Atualiza os dados de um usuário existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.atualizar(id, usuario));
    }

    /**
     * DELETE /api/usuarios/{id} - Remove um usuário pelo ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
