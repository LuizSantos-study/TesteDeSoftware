import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { CadastroFuncionarioRequest, Funcionario } from './models';

@Injectable({ providedIn: 'root' })
export class FuncionarioService {
  constructor(private readonly http: HttpClient) {}

  cadastrar(request: CadastroFuncionarioRequest) {
    return this.http.post<Funcionario>(`${API_URL}/funcionarios`, request);
  }
}
