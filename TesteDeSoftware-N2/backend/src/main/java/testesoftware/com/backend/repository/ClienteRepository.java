package testesoftware.com.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testesoftware.com.backend.model.Cliente;

import java.util.Optional;

/**
 * Repositório JPA para persistência e consulta de Clientes.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCpf(String cpf);

    boolean existsByCpf(String cpf);
}
