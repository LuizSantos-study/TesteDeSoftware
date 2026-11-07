package testesoftware.com.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import testesoftware.com.backend.enums.FuncaoFuncionario;
import testesoftware.com.backend.model.Funcionario;
import testesoftware.com.backend.model.Usuario;
import testesoftware.com.backend.repository.FuncionarioRepository;
import testesoftware.com.backend.repository.UsuarioRepository;

import java.util.List;
import java.util.Objects;

/**
 * Camada de serviço responsável pelo CRUD de Usuários e pela regra de negócio de Login / Autenticação.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final FuncionarioRepository funcionarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, FuncionarioRepository funcionarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    /**
     * Resolve o funcionário vinculado buscando-o no banco pelo ID.
     * Evita que um objeto "solto" vindo do JSON (ex.: id = 0 do exemplo do Swagger) seja usado diretamente.
     */
    private Funcionario resolverFuncionario(Funcionario funcionario) {
        if (funcionario == null || funcionario.getId() == null || funcionario.getId() <= 0) {
            return null;
        }
        return funcionarioRepository.findById(funcionario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com ID: " + funcionario.getId()));
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));
    }

    public Usuario buscarPorLogin(String login) {
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com login: " + login));
    }

    /**
     * Regra de negócio de autenticação simples para a tela de Login:
     * - Aceita login ou e-mail no campo de identificação;
     * - Verifica se o usuário existe no banco;
     * - Verifica se o usuário está ativo;
     * - Confere se a senha informada é válida.
     */
    public Usuario autenticar(String loginOuEmail, String senhaInformada) {
        if (loginOuEmail == null || loginOuEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("O login ou e-mail é obrigatório para autenticação.");
        }
        if (senhaInformada == null || senhaInformada.trim().isEmpty()) {
            throw new IllegalArgumentException("A senha é obrigatória para autenticação.");
        }

        String identificador = loginOuEmail.trim();
        Usuario usuario = usuarioRepository.findByLogin(identificador)
                .or(() -> usuarioRepository.findByEmail(identificador))
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas: login ou senha incorretos."));

        if (!usuario.isAtivo()) {
            throw new IllegalStateException("Usuário inativo não pode realizar login.");
        }

        if (!Objects.equals(usuario.getSenha(), senhaInformada)) {
            throw new IllegalArgumentException("Credenciais inválidas: login ou senha incorretos.");
        }

        return usuario;
    }

    /**
     * Cria uma nova conta de usuário (auto-cadastro ou cadastro via CRUD).
     * Caso o perfil não seja informado, assume OPERADOR_CAIXA e ativo = true por padrão.
     */
    @Transactional
    public Usuario criar(Usuario usuario) {
        if (usuario != null && usuario.getPerfil() == null) {
            usuario.setPerfil(FuncaoFuncionario.OPERADOR_CAIXA);
        }
        validarUsuario(usuario);

        if (usuarioRepository.existsByLogin(usuario.getLogin())) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com o login: " + usuario.getLogin());
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com o e-mail: " + usuario.getEmail());
        }

        // Na criação o ID é sempre gerado pelo banco: ignora qualquer "id" enviado no JSON.
        // Sem isso o save() executa merge() em vez de persist() e lança StaleObjectStateException.
        usuario.setId(null);
        usuario.setFuncionario(resolverFuncionario(usuario.getFuncionario()));

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizar(Long id, Usuario dadosAtualizados) {
        Usuario existente = buscarPorId(id);
        validarUsuario(dadosAtualizados);

        if (!existente.getLogin().equals(dadosAtualizados.getLogin())
                && usuarioRepository.existsByLogin(dadosAtualizados.getLogin())) {
            throw new IllegalArgumentException("Já existe outro usuário cadastrado com o login: " + dadosAtualizados.getLogin());
        }

        if (!existente.getEmail().equals(dadosAtualizados.getEmail())
                && usuarioRepository.existsByEmail(dadosAtualizados.getEmail())) {
            throw new IllegalArgumentException("Já existe outro usuário cadastrado com o e-mail: " + dadosAtualizados.getEmail());
        }

        existente.setNome(dadosAtualizados.getNome());
        existente.setLogin(dadosAtualizados.getLogin());
        existente.setEmail(dadosAtualizados.getEmail());
        existente.setSenha(dadosAtualizados.getSenha());
        existente.setPerfil(dadosAtualizados.getPerfil() != null ? dadosAtualizados.getPerfil() : FuncaoFuncionario.OPERADOR_CAIXA);
        existente.setAtivo(dadosAtualizados.isAtivo());
        existente.setFuncionario(resolverFuncionario(dadosAtualizados.getFuncionario()));

        return usuarioRepository.save(existente);
    }

    @Transactional
    public void deletar(Long id) {
        Usuario existente = buscarPorId(id);
        usuarioRepository.delete(existente);
    }

    public void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Os dados do usuário não podem ser nulos.");
        }
        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do usuário é obrigatório.");
        }
        if (usuario.getLogin() == null || usuario.getLogin().trim().isEmpty()) {
            throw new IllegalArgumentException("O login do usuário é obrigatório.");
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("O e-mail do usuário é obrigatório.");
        }
        if (usuario.getSenha() == null || usuario.getSenha().trim().length() < 4) {
            throw new IllegalArgumentException("A senha do usuário deve conter no mínimo 4 caracteres.");
        }
    }
}
