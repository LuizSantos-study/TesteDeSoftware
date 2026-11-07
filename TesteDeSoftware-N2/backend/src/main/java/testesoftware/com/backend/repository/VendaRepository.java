package testesoftware.com.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testesoftware.com.backend.enums.StatusVenda;
import testesoftware.com.backend.model.Venda;

import java.util.List;

/**
 * Repositório JPA para persistência e consulta de Vendas.
 */
@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    List<Venda> findByStatus(StatusVenda status);

    List<Venda> findByClienteId(Long clienteId);
}
