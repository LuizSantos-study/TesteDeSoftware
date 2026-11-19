package testesoftware.com.backend.service;

import org.springframework.stereotype.Service;
import testesoftware.com.backend.model.Produto;
import testesoftware.com.backend.repository.ProdutoRepository;

import java.util.List;

/**
 * Camada de serviço responsável pelas consultas de Produtos no PDV.
 */
@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com ID: " + id));
    }
    
}
