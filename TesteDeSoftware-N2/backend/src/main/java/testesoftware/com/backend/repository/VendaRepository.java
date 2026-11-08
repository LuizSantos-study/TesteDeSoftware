package testesoftware.com.backend.repository;

import org.springframework.data.repository.Repository;
import testesoftware.com.backend.enums.StatusVenda;
import testesoftware.com.backend.model.Venda;

import java.util.List;
import java.util.Optional;

/**
 * Repositório de Vendas. Estende apenas {@link Repository} (e não JpaRepository)
 * de propósito: não expõe nenhum método de exclusão física, pois vendas são canceladas
 * via status para fins de auditoria.
 */
@org.springframework.stereotype.Repository
public interface VendaRepository extends Repository<Venda, Long> {

    Venda save(Venda venda);

    Optional<Venda> findById(Long id);

    List<Venda> findAll();

    List<Venda> findByStatus(StatusVenda status);

    List<Venda> findByClienteId(Long clienteId);
}
