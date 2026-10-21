import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Cosif, Produto } from '../models/movimento';
@Injectable({ providedIn: 'root' })
export class ProdutosApi {
  private readonly http = inject(HttpClient);
  produtos() {
    return this.http.get<Produto[]>('/api/v1/produtos');
  }
  cosifs(codigo: string) {
    return this.http.get<Cosif[]>(`/api/v1/produtos/${encodeURIComponent(codigo)}/cosifs`);
  }
}
