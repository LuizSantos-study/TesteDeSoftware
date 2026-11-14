import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { Cliente, FormaPagamento, Funcionario, Produto } from './models';

@Injectable({ providedIn: 'root' })
export class CatalogService {
  constructor(private readonly http: HttpClient) {}

  produtos() {
    return this.http.get<Produto[]>(`${API_URL}/produtos`);
  }

  clientes() {
    return this.http.get<Cliente[]>(`${API_URL}/clientes`);
  }

  clientePorCpf(cpf: string) {
    return this.http.get<Cliente>(`${API_URL}/clientes/cpf/${encodeURIComponent(cpf)}`);
  }

  formasPagamentoAtivas() {
    return this.http.get<FormaPagamento[]>(`${API_URL}/formas-pagamento/ativas`);
  }

  farmaceuticos() {
    return this.http.get<Funcionario[]>(`${API_URL}/funcionarios/farmaceuticos`);
  }
}
