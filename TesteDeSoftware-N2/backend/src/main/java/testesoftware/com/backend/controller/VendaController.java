package testesoftware.com.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import testesoftware.com.backend.dto.AdicionarItemRequest;
import testesoftware.com.backend.dto.DescontoRequest;
import testesoftware.com.backend.dto.VendaRequest;
import testesoftware.com.backend.dto.VendaResponse;
import testesoftware.com.backend.model.Produto;
import testesoftware.com.backend.model.Venda;
import testesoftware.com.backend.service.ProdutoService;
import testesoftware.com.backend.service.VendaService;

import java.util.List;

/**
 * Controller REST para Vendas e operações do PDV (GET, POST, PUT; sem exclusão física).
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

    @GetMapping
    public ResponseEntity<List<VendaResponse>> listarTodas() {
        return ResponseEntity.ok(vendaService.listarTodas().stream().map(VendaResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(VendaResponse.from(vendaService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<VendaResponse> criar(@Valid @RequestBody VendaRequest venda) {
        Venda criada = vendaService.criar(venda);
        return ResponseEntity.status(HttpStatus.CREATED).body(VendaResponse.from(criada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody VendaRequest venda) {
        return ResponseEntity.ok(VendaResponse.from(vendaService.atualizar(id, venda)));
    }

    @PostMapping("/{id}/itens")
    public ResponseEntity<VendaResponse> adicionarItem(@PathVariable Long id, @Valid @RequestBody AdicionarItemRequest body) {
        Venda venda = vendaService.buscarPorId(id);
        Produto produto = produtoService.buscarPorId(body.produtoId());
        vendaService.adicionarItem(venda, produto, body.quantidade());

        return ResponseEntity.ok(VendaResponse.from(vendaService.salvar(venda)));
    }

    @PutMapping("/{id}/itens/{itemId}/cancelar")
    public ResponseEntity<VendaResponse> cancelarItem(@PathVariable Long id, @PathVariable Long itemId) {
        return ResponseEntity.ok(VendaResponse.from(vendaService.cancelarItemPorId(id, itemId)));
    }

    @PutMapping("/{id}/desconto")
    public ResponseEntity<VendaResponse> aplicarDesconto(@PathVariable Long id, @Valid @RequestBody DescontoRequest body) {
        Venda venda = vendaService.buscarPorId(id);
        vendaService.aplicarDesconto(venda, body.desconto());
        return ResponseEntity.ok(VendaResponse.from(vendaService.salvar(venda)));
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<VendaResponse> finalizar(@PathVariable Long id) {
        Venda venda = vendaService.buscarPorId(id);
        return ResponseEntity.ok(VendaResponse.from(vendaService.processarFinalizacaoVenda(venda)));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<VendaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(VendaResponse.from(vendaService.cancelarVendaPorId(id)));
    }

}
