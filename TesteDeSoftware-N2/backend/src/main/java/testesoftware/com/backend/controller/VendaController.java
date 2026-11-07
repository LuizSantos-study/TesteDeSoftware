package testesoftware.com.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import testesoftware.com.backend.model.Produto;
import testesoftware.com.backend.model.Venda;
import testesoftware.com.backend.service.ProdutoService;
import testesoftware.com.backend.service.VendaService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para Vendas e operações do PDV (GET, POST, PUT, DELETE).
 * Permite iniciar/atualizar vendas, adicionar ou cancelar itens, aplicar desconto,
 * finalizar venda e cancelar venda.
 */
@RestController
@RequestMapping("/api/vendas")
@CrossOrigin(origins = "*")
public class VendaController {

    private final VendaService vendaService;
    private final ProdutoService produtoService;

    public VendaController(VendaService vendaService, ProdutoService produtoService) {
        this.vendaService = vendaService;
        this.produtoService = produtoService;
    }

    /**
     * GET /api/vendas - Lista todas as vendas cadastradas.
     */
    @GetMapping
    public ResponseEntity<List<Venda>> listarTodas() {
        return ResponseEntity.ok(vendaService.listarTodas());
    }

    /**
     * GET /api/vendas/{id} - Busca uma venda pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Venda> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.buscarPorId(id));
    }

    /**
     * POST /api/vendas - Cria uma nova venda no PDV (ou já finaliza caso status = FINALIZADA).
     */
    @PostMapping
    public ResponseEntity<Venda> criar(@RequestBody Venda venda) {
        Venda criada = vendaService.criar(venda);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    /**
     * PUT /api/vendas/{id} - Atualiza uma venda existente (itens, desconto, cliente, pagamento, status, etc.).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Venda> atualizar(@PathVariable Long id, @RequestBody Venda venda) {
        return ResponseEntity.ok(vendaService.atualizar(id, venda));
    }

    /**
     * POST /api/vendas/{id}/itens - Adiciona um produto ao carrinho da venda pelo ID do produto e quantidade.
     */
    @PostMapping("/{id}/itens")
    public ResponseEntity<Venda> adicionarItem(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Venda venda = vendaService.buscarPorId(id);
        Long produtoId = Long.valueOf(body.get("produtoId").toString());
        int quantidade = Integer.parseInt(body.getOrDefault("quantidade", 1).toString());

        Produto produto = produtoService.buscarPorId(produtoId);
        vendaService.adicionarItem(venda, produto, quantidade);

        return ResponseEntity.ok(vendaService.salvar(venda));
    }

    /**
     * PUT /api/vendas/{id}/itens/{itemId}/cancelar - Cancela um item específico da venda e recalcula o total.
     */
    @PutMapping("/{id}/itens/{itemId}/cancelar")
    public ResponseEntity<Venda> cancelarItemViaPut(@PathVariable Long id, @PathVariable Long itemId) {
        return ResponseEntity.ok(vendaService.cancelarItemPorId(id, itemId));
    }

    /**
     * DELETE /api/vendas/{id}/itens/{itemId} - Cancela/remove um item específico da venda e recalcula o total.
     */
    @DeleteMapping("/{id}/itens/{itemId}")
    public ResponseEntity<Venda> cancelarItem(@PathVariable Long id, @PathVariable Long itemId) {
        return ResponseEntity.ok(vendaService.cancelarItemPorId(id, itemId));
    }

    /**
     * PUT /api/vendas/{id}/desconto - Aplica desconto na venda validando as regras de negócio.
     */
    @PutMapping("/{id}/desconto")
    public ResponseEntity<Venda> aplicarDesconto(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Venda venda = vendaService.buscarPorId(id);
        BigDecimal desconto = new BigDecimal(body.getOrDefault("desconto", "0").toString());
        vendaService.aplicarDesconto(venda, desconto);
        return ResponseEntity.ok(vendaService.salvar(venda));
    }

    /**
     * PUT /api/vendas/{id}/finalizar (e POST /api/vendas/{id}/finalizar)
     * Finaliza a venda executando validações SNGPC e TEF/POS.
     */
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<Venda> finalizarViaPut(@PathVariable Long id) {
        Venda venda = vendaService.buscarPorId(id);
        return ResponseEntity.ok(vendaService.processarFinalizacaoVenda(venda));
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<Venda> finalizar(@PathVariable Long id) {
        Venda venda = vendaService.buscarPorId(id);
        return ResponseEntity.ok(vendaService.processarFinalizacaoVenda(venda));
    }

    /**
     * PUT /api/vendas/{id}/cancelar (e POST /api/vendas/{id}/cancelar)
     * Cancela a venda alterando seu status para CANCELADA.
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Venda> cancelarViaPut(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.cancelarVendaPorId(id));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Venda> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.cancelarVendaPorId(id));
    }

    /**
     * DELETE /api/vendas/{id} - Exclui uma venda (desde que não esteja FINALIZADA).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        vendaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
