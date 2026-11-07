package testesoftware.com.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testesoftware.com.backend.model.ItemVenda;

import java.util.List;

/**
 * Repositório JPA para persistência e consulta de Itens de Venda.
 */
@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {

    List<ItemVenda> findByVendaId(Long vendaId);
}
