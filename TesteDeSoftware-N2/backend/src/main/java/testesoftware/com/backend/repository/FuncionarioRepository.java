package testesoftware.com.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import testesoftware.com.backend.enums.FuncaoFuncionario;
import testesoftware.com.backend.model.Funcionario;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para persistência e consulta de Funcionários.
 */
@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    List<Funcionario> findByFuncao(FuncaoFuncionario funcao);
}
