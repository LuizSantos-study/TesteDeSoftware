import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { Venda, VendaRequest } from './models';

@Injectable({ providedIn: 'root' })
export class VendaService {
  constructor(private readonly http: HttpClient) {}

  listar() {
    return this.http.get<Venda[]>(`${API_URL}/vendas`);
  }

  buscar(id: number) {
    return this.http.get<Venda>(`${API_URL}/vendas/${id}`);
  }

  criar(request: VendaRequest) {
    return this.http.post<Venda>(`${API_URL}/vendas`, request);
  }

  adicionarItem(id: number, produtoId: number, quantidade: number) {
    return this.http.post<Venda>(`${API_URL}/vendas/${id}/itens`, { produtoId, quantidade });
  }

  aplicarDesconto(id: number, desconto: number) {
    return this.http.put<Venda>(`${API_URL}/vendas/${id}/desconto`, { desconto });
  }

  cancelarItem(id: number, itemId: number) {
    return this.http.put<Venda>(`${API_URL}/vendas/${id}/itens/${itemId}/cancelar`, {});
  }

  finalizar(id: number) {
    return this.http.put<Venda>(`${API_URL}/vendas/${id}/finalizar`, {});
  }

  cancelar(id: number) {
    return this.http.put<Venda>(`${API_URL}/vendas/${id}/cancelar`, {});
  }
}
