import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_URL } from './api.config';
import { Funcionario, LoginRequest, LoginResponse } from './models';

const STORAGE_KEY = 'farmacia.funcionario';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly currentUser = signal<Funcionario | null>(this.readUser());

  constructor(private readonly http: HttpClient) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_URL}/funcionarios/login`, credentials).pipe(
      tap((response) => {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(response.funcionario));
        this.currentUser.set(response.funcionario);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.currentUser.set(null);
  }

  isAuthenticated(): boolean {
    return this.currentUser() !== null;
  }

  private readUser(): Funcionario | null {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (!stored) {
      return null;
    }

    try {
      return JSON.parse(stored) as Funcionario;
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }
}
