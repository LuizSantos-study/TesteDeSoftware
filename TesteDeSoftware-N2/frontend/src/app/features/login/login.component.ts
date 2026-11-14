import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { FuncionarioService } from '../../core/funcionario.service';
import { FuncaoFuncionario } from '../../core/models';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly formBuilder = inject(FormBuilder);
  readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required]],
  });
  readonly registration = this.formBuilder.nonNullable.group({
    nome: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required, Validators.minLength(4)]],
    cpf: [''],
    cargo: ['OPERADOR_CAIXA' as FuncaoFuncionario, Validators.required],
    crf: [''],
  });
  submitting = false;
  registering = false;
  error = '';
  success = '';

  private readonly auth = inject(AuthService);
  private readonly funcionarioService = inject(FuncionarioService);
  private readonly router = inject(Router);

  constructor() {
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.error = '';
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/app/dashboard']),
      error: (error: Error) => { this.error = error.message; this.submitting = false; },
    });
  }

  register(): void {
    if (this.registration.invalid) {
      this.registration.markAllAsTouched();
      return;
    }
    const value = this.registration.getRawValue();
    if (value.cargo === 'FARMACEUTICO' && !value.crf.trim()) {
      this.error = 'Informe o CRF para cadastrar um farmacêutico.';
      return;
    }
    this.registering = true;
    this.error = '';
    this.funcionarioService.cadastrar({ ...value, cpf: value.cpf || undefined, crf: value.crf || undefined }).subscribe({
      next: () => { this.success = 'Cadastro realizado. Agora entre com seu e-mail e senha.'; this.registering = false; this.registration.reset({ cargo: 'OPERADOR_CAIXA' }); },
      error: (error: Error) => { this.error = error.message; this.registering = false; },
    });
  }
}
