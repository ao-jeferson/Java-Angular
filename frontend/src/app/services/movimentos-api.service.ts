import { API_BASE_URL } from './api-url';
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Competencia, Movimento, NovoMovimento } from '../models/movimento';
@Injectable({ providedIn: 'root' })
export class MovimentosApi {
  private readonly http = inject(HttpClient);
  private readonly apiBase = inject(API_BASE_URL);
  consultar(c: Competencia) {
    return this.http.get<Movimento[]>(`${this.apiBase}/movimentos`, {
      params: { mes: c.mes, ano: c.ano },
    });
  }
  incluir(movimento: NovoMovimento) {
    return this.http.post<Movimento>(`${this.apiBase}/movimentos`, movimento);
  }
}
