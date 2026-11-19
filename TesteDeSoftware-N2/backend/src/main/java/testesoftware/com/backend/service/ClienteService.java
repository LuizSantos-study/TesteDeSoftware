package testesoftware.com.backend.service;

import org.springframework.stereotype.Service;
import testesoftware.com.backend.enums.PlanoCliente;
import testesoftware.com.backend.model.Cliente;
import testesoftware.com.backend.repository.ClienteRepository;

import java.util.List;

/**
 * Camada de serviço responsável pelas consultas e regras de negócio de Clientes no PDV.
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com ID: " + id));
    }

    public Cliente buscarPorCpf(String cpf) {
        return clienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com CPF: " + cpf));
    }

    public boolean clientePossuiPlano(Cliente cliente) {
        return cliente != null && cliente.getPlano() != null && cliente.getPlano() != PlanoCliente.NENHUM;
    }
}
