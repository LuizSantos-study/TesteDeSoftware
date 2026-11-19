package testesoftware.com.backend.service;

import org.springframework.stereotype.Service;
import testesoftware.com.backend.enums.ModalidadeIntegracao;
import testesoftware.com.backend.model.FormaPagamento;
import testesoftware.com.backend.repository.FormaPagamentoRepository;

import java.util.List;

/**
 * Camada de serviço responsável pelas consultas e regras de negócio de Formas de Pagamento no PDV.
 */
@Service
public class FormaPagamentoService {

    private final FormaPagamentoRepository formaPagamentoRepository;

    public FormaPagamentoService(FormaPagamentoRepository formaPagamentoRepository) {
        this.formaPagamentoRepository = formaPagamentoRepository;
    }

    public List<FormaPagamento> listarTodas() {
        return formaPagamentoRepository.findAll();
    }

    public List<FormaPagamento> listarAtivas() {
        return formaPagamentoRepository.findByAtivoTrue();
    }

    public FormaPagamento buscarPorId(Long id) {
        return formaPagamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Forma de pagamento não encontrada com ID: " + id));
    }

    public boolean isTef(FormaPagamento formaPagamento) {
        return formaPagamento != null && formaPagamento.getTipoPagamento() == ModalidadeIntegracao.TEF;
    }

    public boolean isPos(FormaPagamento formaPagamento) {
        return formaPagamento != null && formaPagamento.getTipoPagamento() == ModalidadeIntegracao.POS;
    }

    public boolean requerIntegracaoEletronica(FormaPagamento formaPagamento) {
        return isTef(formaPagamento) || isPos(formaPagamento);
    }
}
