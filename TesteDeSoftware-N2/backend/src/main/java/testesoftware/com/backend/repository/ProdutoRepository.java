package testesoftware.com.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testesoftware.com.backend.model.Produto;

/**
 * Repositório JPA para consulta de Produtos no banco de dados.
 */
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
