import { API_BASE_URL } from '../services/api-url';
import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
interface TokenResponse {
  accessToken: string;
  expiresIn: number;
  tokenType: string;
}
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiBase = inject(API_BASE_URL);
  private readonly router = inject(Router);
  readonly token = signal<string | null>(null);
  private expiresAt = 0;
  private timer?: ReturnType<typeof setTimeout>;
  login(username: string, password: string) {
    return this.http.post<TokenResponse>(`${this.apiBase}/auth/login`, { username, password }).pipe(
      tap((response) => {
        this.clear();
        this.token.set(response.accessToken);
        this.expiresAt = Date.now() + response.expiresIn * 1000;
        this.timer = setTimeout(() => this.logout(), response.expiresIn * 1000);
      }),
    );
  }
  authenticated() {
    return this.token() !== null && Date.now() < this.expiresAt;
  }
  clear() {
    if (this.timer) clearTimeout(this.timer);
    this.token.set(null);
    this.expiresAt = 0;
  }
  logout() {
    this.clear();
    void this.router.navigate(['/login']);
  }
}
