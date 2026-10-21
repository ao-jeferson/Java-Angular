import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { apiError } from '../auth/api-error';
@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule],
  template: ` <main class="login-shell">
    <section class="login-story">
      <a class="wordmark" href="/">M<span>↗</span> Movimento</a>
      <div>
        <p class="eyebrow">CONTROLE COM CLAREZA</p>
        <h1>Cada movimento.<br />Todo o controle.</h1>
        <p class="story-copy">
          Organize seus lançamentos manuais com precisão e uma visão clara de cada competência.
        </p>
      </div>
      <p class="quiet">Java + Angular · Desafio técnico</p>
    </section>
    <section class="login-area">
      <form class="login-card" [formGroup]="form" (ngSubmit)="submit()">
        <p class="eyebrow">ACESSO SEGURO</p>
        <h2>Entre no Movimento</h2>
        <p class="muted">Use suas credenciais para continuar.</p>
        <label for="username">Usuário</label
        ><input id="username" formControlName="username" autocomplete="username" autofocus />
        <label for="password">Senha</label
        ><input
          id="password"
          type="password"
          formControlName="password"
          autocomplete="current-password"
        />
        @if (error()) {
          <p class="alert error" role="alert">{{ error() }}</p>
        }
        <button class="primary full" type="submit" [disabled]="loading()">
          {{ loading() ? 'Entrando…' : 'Entrar →' }}
        </button>
        <p class="quiet">Sua sessão é protegida e expira automaticamente.</p>
      </form>
    </section>
  </main>`,
})
export class LoginPage {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });
  submit() {
    if (this.loading()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Informe usuário e senha.');
      return;
    }
    this.loading.set(true);
    this.error.set('');
    const { username, password } = this.form.getRawValue();
    this.auth
      .login(username, password)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          this.form.reset();
          void this.router.navigate(['/movimentos']);
        },
        error: (error) => this.error.set(apiError(error)),
      });
  }
}
