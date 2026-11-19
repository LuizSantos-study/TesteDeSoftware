import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Venda, StatusVenda } from '../../core/models';
import { VendaService } from '../../core/venda.service';

@Component({
  selector: 'app-sales',
  imports: [CommonModule, CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './sales.component.html',
  styleUrl: './sales.component.scss',
})
export class SalesComponent implements OnInit {
  sales: Venda[] = [];
  filter: StatusVenda | 'TODAS' = 'TODAS';
  loading = true;
  error = '';
  processingSaleId: number | null = null;
  confirmingSale: Venda | null = null;

  constructor(
    private readonly saleService: VendaService,
    private readonly changeDetector: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.load();
  }
  load(): void {
    this.saleService.listar().subscribe({
      next: (items) => {
        this.sales = items;
        this.loading = false;
        this.changeDetector.detectChanges();
      },
      error: (error: Error) => {
        this.error = error.message;
        this.loading = false;
        this.changeDetector.detectChanges();
      },
    });
  }
  get filteredSales(): Venda[] {
    return this.filter === 'TODAS'
      ? this.sales
      : this.sales.filter((sale) => sale.status === this.filter);
  }

  finalizeSale(sale: Venda): void {
    this.runStatusAction(sale, 'finalize');
  }

  cancelSale(sale: Venda): void {
    this.confirmingSale = sale;
  }

  closeCancelModal(): void {
    this.confirmingSale = null;
  }

  confirmCancel(): void {
    if (!this.confirmingSale) {
      return;
    }
    const sale = this.confirmingSale;
    this.confirmingSale = null;
    this.runStatusAction(sale, 'cancel');
  }

  private runStatusAction(sale: Venda, action: 'finalize' | 'cancel'): void {
    this.processingSaleId = sale.id;
    this.error = '';
    const request =
      action === 'finalize'
        ? this.saleService.finalizar(sale.id)
        : this.saleService.cancelar(sale.id);

    request.subscribe({
      next: (updatedSale) => {
        const index = this.sales.findIndex((item) => item.id === updatedSale.id);
        if (index >= 0) {
          this.sales[index] = updatedSale;
        }
        this.processingSaleId = null;
        this.changeDetector.detectChanges();
      },
      error: (error: Error) => {
        this.error = error.message;
        this.processingSaleId = null;
        this.changeDetector.detectChanges();
      },
    });
  }
}
