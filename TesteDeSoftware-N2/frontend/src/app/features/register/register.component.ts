import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { FuncionarioService } from '../../core/funcionario.service';
import { FuncaoFuncionario } from '../../core/models';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: '../login/login.component.scss',
})
export class RegisterComponent {
  private readonly formBuilder = inject(FormBuilder);
  readonly form = this.formBuilder.nonNullable.group({
    nome: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required, Validators.minLength(4)]],
    cpf: [''],
    cargo: ['OPERADOR_CAIXA' as FuncaoFuncionario, Validators.required],
    crf: [''],
  });
  submitting = false;
  error = '';

  private readonly funcionarioService = inject(FuncionarioService);
  private readonly router = inject(Router);

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    if (value.cargo === 'FARMACEUTICO' && !value.crf.trim()) {
      this.error = 'Informe o CRF para cadastrar um farmacêutico.';
      return;
    }
    this.submitting = true;
    this.error = '';
    this.funcionarioService
      .cadastrar({ ...value, cpf: value.cpf || undefined, crf: value.crf || undefined })
      .subscribe({
        next: () => this.router.navigate(['/login'], { queryParams: { cadastrado: '1' } }),
        error: (error: Error) => {
          this.error = error.message;
          this.submitting = false;
        },
      });
  }
}
