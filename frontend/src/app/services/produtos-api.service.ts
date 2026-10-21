import { API_BASE_URL } from './api-url';
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Cosif, Produto } from '../models/movimento';
@Injectable({ providedIn: 'root' })
export class ProdutosApi {
  private readonly http = inject(HttpClient);
  private readonly apiBase = inject(API_BASE_URL);
  produtos() {
    return this.http.get<Produto[]>(`${this.apiBase}/produtos`);
  }
  cosifs(codigo: string) {
    return this.http.get<Cosif[]>(`${this.apiBase}/produtos/${encodeURIComponent(codigo)}/cosifs`);
  }
}
