import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Competencia, Movimento, NovoMovimento } from '../models/movimento';
@Injectable({ providedIn: 'root' })
export class MovimentosApi {
  private readonly http = inject(HttpClient);
  consultar(c: Competencia) {
    return this.http.get<Movimento[]>('/api/v1/movimentos', { params: { mes: c.mes, ano: c.ano } });
  }
  incluir(movimento: NovoMovimento) {
    return this.http.post<Movimento>('/api/v1/movimentos', movimento);
  }
}
