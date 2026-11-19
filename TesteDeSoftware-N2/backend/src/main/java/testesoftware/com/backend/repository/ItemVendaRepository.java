package testesoftware.com.backend.repository;

import org.springframework.data.repository.Repository;
import testesoftware.com.backend.model.ItemVenda;

import java.util.List;
import java.util.Optional;

/**
 * Repositório de Itens de Venda. Estende apenas {@link Repository} (e não JpaRepository)
 * de propósito: não expõe nenhum método de exclusão física, pois itens são cancelados
 * via status para fins de auditoria.
 */
@org.springframework.stereotype.Repository
public interface ItemVendaRepository extends Repository<ItemVenda, Long> {

    ItemVenda save(ItemVenda item);

    Optional<ItemVenda> findById(Long id);

    List<ItemVenda> findAll();

    List<ItemVenda> findByVendaId(Long vendaId);
}
