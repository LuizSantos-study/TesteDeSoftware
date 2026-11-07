package testesoftware.com.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testesoftware.com.backend.model.Usuario;

import java.util.Optional;

/**
 * Repositório JPA para persistência e autenticação de Usuários do sistema.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    Optional<Usuario> findByEmail(String email);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);
}
