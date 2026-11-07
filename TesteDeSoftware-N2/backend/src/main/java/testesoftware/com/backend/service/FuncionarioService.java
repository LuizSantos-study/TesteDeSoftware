package testesoftware.com.backend.service;

import org.springframework.stereotype.Service;
import testesoftware.com.backend.enums.FuncaoFuncionario;
import testesoftware.com.backend.model.Funcionario;
import testesoftware.com.backend.repository.FuncionarioRepository;

import java.util.List;

/**
 * Camada de serviço responsável pelas consultas e regras de negócio de Funcionários no PDV.
 */
@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    public List<Funcionario> listarTodos() {
        return funcionarioRepository.findAll();
    }

    public Funcionario buscarPorId(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com ID: " + id));
    }

    public List<Funcionario> listarFarmaceuticos() {
        return funcionarioRepository.findByFuncao(FuncaoFuncionario.FARMACEUTICO);
    }

    public boolean isFarmaceutico(Funcionario funcionario) {
        return funcionario != null && funcionario.getFuncao() == FuncaoFuncionario.FARMACEUTICO;
    }

    public boolean isGerente(Funcionario funcionario) {
        return funcionario != null && funcionario.getFuncao() == FuncaoFuncionario.GERENTE;
    }

    public boolean isOperadorCaixa(Funcionario funcionario) {
        return funcionario != null && funcionario.getFuncao() == FuncaoFuncionario.OPERADOR_CAIXA;
    }
}
