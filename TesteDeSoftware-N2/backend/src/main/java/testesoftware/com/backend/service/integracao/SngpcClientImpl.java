package testesoftware.com.backend.service.integracao;

import org.springframework.stereotype.Service;
import testesoftware.com.backend.model.ItemVenda;

import java.util.List;

/**
 * Implementação padrão do cliente SNGPC (ANVISA) para execução da API.
 */
@Service
public class SngpcClientImpl implements SngpcClient {

    @Override
    public boolean consultarReceita(String numeroReceita, String cpfCliente) {
        return numeroReceita != null
                && !numeroReceita.trim().isEmpty()
                && !numeroReceita.equalsIgnoreCase("INVALIDA");
    }

    @Override
    public String emitirNotificacaoSNGPC(String numeroReceita, String crfFarmaceutico, List<ItemVenda> itensControlados) {
        return "SNGPC-2026-" + Math.abs(numeroReceita != null ? numeroReceita.hashCode() : 1000);
    }
}
