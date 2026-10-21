import { Component, Input } from '@angular/core';
import { Movimento } from '../models/movimento';
export function money(valor: string): string {
  const [integer, decimal = '00'] = valor.split('.');
  return 'R$ ' + integer.replace(/\B(?=(\d{3})+(?!\d))/g, '.') + ',' + decimal.padEnd(2, '0');
}
@Component({
  selector: 'app-movimentos-grid',
  template: `<div class="table-wrap">
    <table>
      <caption class="sr-only">
        Movimentos da competência selecionada
      </caption>
      <thead>
        <tr>
          <th>Mês</th>
          <th>Ano</th>
          <th>Código do produto</th>
          <th>Descrição do produto</th>
          <th>Nº lançamento</th>
          <th>Descrição</th>
          <th class="number">Valor</th>
        </tr>
      </thead>
      <tbody>
        @for (m of movimentos; track m.numeroLancamento) {
          <tr>
            <td>{{ m.mes.toString().padStart(2, '0') }}</td>
            <td>{{ m.ano }}</td>
            <td>
              <span class="code">{{ m.codigoProduto }}</span>
            </td>
            <td>{{ m.descricaoProduto || '—' }}</td>
            <td class="tabular">{{ m.numeroLancamento.padStart(3, '0') }}</td>
            <td>{{ m.descricao }}</td>
            <td class="number tabular">{{ formatMoney(m.valor) }}</td>
          </tr>
        }
      </tbody>
    </table>
  </div>`,
})
export class MovimentosGrid {
  @Input() movimentos: Movimento[] = [];
  readonly formatMoney = money;
}
