package testesoftware.com.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import testesoftware.com.backend.enums.MeioPagamento;
import testesoftware.com.backend.enums.ModalidadeIntegracao;

import java.util.Objects;

/**
 * Entidade JPA que representa a Forma de Pagamento configurada no sistema da farmácia.
 */
@Entity
@Table(name = "formas_pagamento")
public class FormaPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A descrição da forma de pagamento é obrigatória.")
    @Column(nullable = false, length = 100)
    private String descricao;

    @NotNull(message = "O meio de pagamento é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MeioPagamento tipo;

    @NotNull(message = "A modalidade de integração é obrigatória.")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pagamento", nullable = false, length = 30)
    private ModalidadeIntegracao tipoPagamento;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "permite_parcelamento", nullable = false)
    private boolean permiteParcelamento;

    public FormaPagamento() {
        this.ativo = true;
        this.tipoPagamento = ModalidadeIntegracao.NAO_APLICAVEL;
    }

    public FormaPagamento(Long id, String descricao, MeioPagamento tipo,
                          ModalidadeIntegracao tipoPagamento, boolean ativo, boolean permiteParcelamento) {
        this.id = id;
        this.descricao = descricao;
        this.tipo = tipo;
        this.tipoPagamento = tipoPagamento != null ? tipoPagamento : ModalidadeIntegracao.NAO_APLICAVEL;
        this.ativo = ativo;
        this.permiteParcelamento = permiteParcelamento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public MeioPagamento getTipo() {
        return tipo;
    }

    public void setTipo(MeioPagamento tipo) {
        this.tipo = tipo;
    }

    public ModalidadeIntegracao getTipoPagamento() {
        return tipoPagamento;
    }

    public void setTipoPagamento(ModalidadeIntegracao tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isPermiteParcelamento() {
        return permiteParcelamento;
    }

    public void setPermiteParcelamento(boolean permiteParcelamento) {
        this.permiteParcelamento = permiteParcelamento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormaPagamento that = (FormaPagamento) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FormaPagamento{" +
                "id=" + id +
                ", descricao='" + descricao + '\'' +
                ", tipo=" + tipo +
                ", tipoPagamento=" + tipoPagamento +
                ", ativo=" + ativo +
                ", permiteParcelamento=" + permiteParcelamento +
                '}';
    }
}
