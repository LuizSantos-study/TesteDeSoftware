package testesoftware.com.backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import testesoftware.com.backend.enums.StatusVenda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidade JPA que representa uma Venda na farmácia.
 */
@Entity
@Table(name = "vendas")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "forma_pagamento_id")
    private FormaPagamento formaPagamento;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemVenda> itens = new ArrayList<>();

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal desconto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusVenda status;

    @Column(name = "numero_receita", length = 60)
    private String numeroReceita;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "farmaceutico_responsavel_id")
    private Funcionario farmaceuticoResponsavel;

    public Venda() {
        this.dataHora = LocalDateTime.now();
        this.status = StatusVenda.PENDENTE;
        this.desconto = BigDecimal.ZERO;
        this.valorTotal = BigDecimal.ZERO;
    }

    public Venda(Long id, Cliente cliente, Funcionario funcionario, FormaPagamento formaPagamento) {
        this();
        this.id = id;
        this.cliente = cliente;
        this.funcionario = funcionario;
        this.formaPagamento = formaPagamento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public List<ItemVenda> getItens() {
        return itens;
    }

    public void setItens(List<ItemVenda> itens) {
        this.itens = itens != null ? itens : new ArrayList<>();
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public StatusVenda getStatus() {
        return status;
    }

    public void setStatus(StatusVenda status) {
        this.status = status;
    }

    public String getNumeroReceita() {
        return numeroReceita;
    }

    public void setNumeroReceita(String numeroReceita) {
        this.numeroReceita = numeroReceita;
    }

    public Funcionario getFarmaceuticoResponsavel() {
        return farmaceuticoResponsavel;
    }

    public void setFarmaceuticoResponsavel(Funcionario farmaceuticoResponsavel) {
        this.farmaceuticoResponsavel = farmaceuticoResponsavel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Venda venda = (Venda) o;
        return Objects.equals(id, venda.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Venda{" +
                "id=" + id +
                ", dataHora=" + dataHora +
                ", cliente=" + (cliente != null ? cliente.getNome() : null) +
                ", funcionario=" + (funcionario != null ? funcionario.getNome() : null) +
                ", formaPagamento=" + (formaPagamento != null ? formaPagamento.getDescricao() : null) +
                ", itens=" + (itens != null ? itens.size() : 0) +
                ", valorTotal=" + valorTotal +
                ", desconto=" + desconto +
                ", status=" + status +
                '}';
    }
}
