package testesoftware.com.backend.service.integracao;

import testesoftware.com.backend.model.ItemVenda;

import java.util.List;

/**
 * Interface de integração com o Sistema Nacional de Gerenciamento de Produtos Controlados (SNGPC - ANVISA).
 * Permite simular a consulta de receitas e emissão de notificações via Mock Objects (Mockito).
 */
public interface SngpcClient {

    boolean consultarReceita(String numeroReceita, String cpfCliente);

    String emitirNotificacaoSNGPC(String numeroReceita, String crfFarmaceutico, List<ItemVenda> itensControlados);
}
