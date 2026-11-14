import { CommonModule, CurrencyPipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CatalogService } from '../../core/catalog.service';
import { Cliente, Produto } from '../../core/models';

@Component({
  selector: 'app-catalog',
  imports: [CommonModule, CurrencyPipe, FormsModule],
  templateUrl: './catalog.component.html',
  styleUrl: './catalog.component.scss',
})
export class CatalogComponent implements OnInit {
  products: Produto[] = [];
  customers: Cliente[] = [];
  searchProduct = '';
  searchCustomer = '';
  loading = true;
  error = '';

  constructor(
    private readonly catalog: CatalogService,
    private readonly changeDetector: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
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
  }

  get filteredProducts(): Produto[] { const term = this.searchProduct.toLowerCase().trim(); return this.products.filter((item) => !term || `${item.nome} ${item.principioAtivo}`.toLowerCase().includes(term)); }
  get filteredCustomers(): Cliente[] { const term = this.searchCustomer.toLowerCase().trim(); return this.customers.filter((item) => !term || `${item.nome} ${item.cpf}`.toLowerCase().includes(term)); }
  private showError = (error: Error): void => {
    this.error = error.message;
    this.loading = false;
    this.changeDetector.detectChanges();
  };
}
