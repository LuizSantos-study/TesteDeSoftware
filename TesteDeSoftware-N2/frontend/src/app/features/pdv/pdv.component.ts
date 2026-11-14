import { CommonModule, CurrencyPipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { CatalogService } from '../../core/catalog.service';
import {
  Cliente,
  FormaPagamento,
  Funcionario,
  Produto,
  Venda,
  VendaRequest,
} from '../../core/models';
import { VendaService } from '../../core/venda.service';

interface CartItem {
  product: Produto;
  quantity: number;
}

@Component({
  selector: 'app-pdv',
  imports: [CommonModule, CurrencyPipe, FormsModule],
  templateUrl: './pdv.component.html',
  styleUrl: './pdv.component.scss',
})
export class PdvComponent implements OnInit {
  products: Produto[] = [];
  customers: Cliente[] = [];
  payments: FormaPagamento[] = [];
  pharmacists: Funcionario[] = [];
  cart: CartItem[] = [];
  selectedProductId: number | null = null;
  quantity = 1;
  selectedCustomerId: number | null = null;
  selectedPaymentId: number | null = null;
  selectedPharmacistId: number | null = null;
  prescription = '';
  discount = 0;
  sale: Venda | null = null;
  loading = true;
  submitting = false;
  message = '';
  error = '';

  // Controle do modal da venda recém-criada
  createdSaleModal: Venda | null = null;

  constructor(
    private readonly catalog: CatalogService,
    private readonly sales: VendaService,
    public readonly auth: AuthService,
    private readonly changeDetector: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    console.group('[PDV] Carregamento inicial');
    console.info('[PDV] Usuário autenticado:', this.auth.currentUser());

    this.catalog.produtos().subscribe({
      next: (items) => {
        this.products = items;
        this.loading = false;
        this.changeDetector.detectChanges();
      },
      error: this.showError,
    });
    this.catalog.clientes().subscribe({
      next: (items) => {
        this.customers = items;
        this.changeDetector.detectChanges();
      },
      error: this.showError,
    });
    this.catalog.formasPagamentoAtivas().subscribe({
      next: (items) => {
        this.payments = items;
        this.changeDetector.detectChanges();
      },
      error: this.showError,
    });
    this.catalog.farmaceuticos().subscribe({
      next: (items) => {
        this.pharmacists = items;
        this.changeDetector.detectChanges();
        console.groupEnd();
      },
      error: this.showError,
    });
  }

  addProduct(): void {
    const product = this.products.find((item) => item.id === Number(this.selectedProductId));
    if (!product || this.quantity < 1) return;
    const existing = this.cart.find((item) => item.product.id === product.id);
    if (existing) existing.quantity += this.quantity;
    else this.cart.push({ product, quantity: this.quantity });
    this.selectedProductId = null;
    this.quantity = 1;
  }

  removeProduct(productId: number): void {
    this.cart = this.cart.filter((item) => item.product.id !== productId);
  }

  get subtotal(): number {
    return this.cart.reduce((total, item) => total + item.product.preco * item.quantity, 0);
  }
  get total(): number {
    return Math.max(0, this.subtotal - Number(this.discount || 0));
  }
  get discountError(): string {
    const value = Number(this.discount || 0);
    if (value < 0) return 'O desconto não pode ser negativo.';
    if (value > this.subtotal) return 'O desconto não pode superar o subtotal.';
    return '';
  }
  get selectedPayment(): FormaPagamento | null {
    return this.payments.find((payment) => payment.id === Number(this.selectedPaymentId)) ?? null;
  }
  get electronicZeroTotalError(): string {
    const paymentMode = this.selectedPayment?.tipoPagamento;
    if (this.total === 0 && (paymentMode === 'POS' || paymentMode === 'TEF')) {
      return 'POS e TEF precisam de um total maior que R$ 0,00. Escolha PIX/dinheiro ou reduza o desconto.';
    }
    return '';
  }
  get hasControlledProduct(): boolean {
    return this.cart.some((item) => item.product.sngpc);
  }

  createSale(): void {
    if (!this.auth.currentUser() || !this.selectedPaymentId || this.cart.length === 0) {
      this.error = 'Selecione a forma de pagamento e adicione pelo menos um produto.';
      return;
    }
    if (this.discountError) {
      this.error = this.discountError;
      return;
    }
    if (this.electronicZeroTotalError) {
      this.error = this.electronicZeroTotalError;
      return;
    }
    if (this.hasControlledProduct && (!this.prescription.trim() || !this.selectedPharmacistId)) {
      this.error = 'Medicamentos controlados exigem receita e farmacêutico responsável.';
      return;
    }
    const request: VendaRequest = {
      clienteId: this.selectedCustomerId || null,
      funcionarioId: this.auth.currentUser()!.id,
      formaPagamentoId: Number(this.selectedPaymentId),
      desconto: Number(this.discount || 0),
      numeroReceita: this.prescription.trim() || null,
      farmaceuticoResponsavelId: this.selectedPharmacistId || null,
      itens: this.cart.map((item) => ({ produtoId: item.product.id, qtdVendida: item.quantity })),
    };
    this.submitting = true;
    this.error = '';
    this.sales.criar(request).subscribe({
      next: (sale) => {
        console.info('[PDV] Venda criada:', sale);

        // 1. Abre o modal com a venda criada
        this.createdSaleModal = sale;
        this.message = `Venda #${sale.id} criada como pendente.`;

        // 2. Limpa os dados da tela imediatamente para a próxima venda
        this.cart = [];
        this.selectedProductId = null;
        this.quantity = 1;
        this.selectedCustomerId = null;
        this.selectedPaymentId = null;
        this.selectedPharmacistId = null;
        this.prescription = '';
        this.discount = 0;
        this.sale = null;

        this.submitting = false;
        this.changeDetector.detectChanges();
      },
      error: this.showError,
    });
  }

  // Ações do Modal
  closeCreatedSaleModal(): void {
    this.createdSaleModal = null;
  }

  modalFinalizeSale(): void {
    if (!this.createdSaleModal) return;
    this.runSaleAction(
      this.sales.finalizar(this.createdSaleModal.id),
      'Venda finalizada com sucesso.',
    );
    this.createdSaleModal = null;
  }

  modalCancelSale(): void {
    if (!this.createdSaleModal) return;
    this.runSaleAction(this.sales.cancelar(this.createdSaleModal.id), 'Venda cancelada.');
    this.createdSaleModal = null;
  }

  private runSaleAction(request: ReturnType<VendaService['finalizar']>, success: string): void {
    this.submitting = true;
    request.subscribe({
      next: (sale) => {
        console.info('[PDV] Operação de venda concluída:', sale);
        this.message = success;
        this.submitting = false;
        this.changeDetector.detectChanges();
      },
      error: this.showError,
    });
  }

  private showError = (error: Error): void => {
    console.error('[PDV] Falha ao carregar ou executar operação:', error);
    this.error = error.message;
    this.loading = false;
    this.submitting = false;
    this.changeDetector.detectChanges();
  };
}
