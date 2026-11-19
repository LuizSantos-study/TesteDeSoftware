package testesoftware.com.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testesoftware.com.backend.model.FormaPagamento;

import java.util.List;

/**
 * Repositório JPA para persistência e consulta de Formas de Pagamento.
 */
@Repository
public interface FormaPagamentoRepository extends JpaRepository<FormaPagamento, Long> {

    List<FormaPagamento> findByAtivoTrue();
}
