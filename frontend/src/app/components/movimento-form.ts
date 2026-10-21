import { Component, Input, Output, EventEmitter, OnChanges, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Competencia, Produto, Cosif, NovoMovimento } from '../models/movimento';
@Component({
  selector: 'app-movimento-form',
  imports: [ReactiveFormsModule],
  template: ` <form [formGroup]="form" (ngSubmit)="submit()" class="movement-form">
    <div class="form-grid">
      <div>
        <label for="mes">Mês</label
        ><input id="mes" type="number" formControlName="mes" min="1" max="12" />
      </div>
      <div>
        <label for="ano">Ano</label
        ><input id="ano" type="number" formControlName="ano" min="1" max="9999" />
      </div>
      <div>
        <label for="produto">Produto</label
        ><select id="produto" formControlName="codigoProduto" (change)="changeProduto()">
          <option value="">Selecione um produto</option>
          @for (p of produtos; track p.codigo) {
            <option [value]="p.codigo">{{ p.descricao || p.codigo }}</option>
          }
        </select>
      </div>
      <div>
        <label for="cosif">Cosif</label
        ><select id="cosif" formControlName="codigoCosif">
          <option value="">{{ loadingCosifs ? 'Carregando…' : 'Selecione um Cosif' }}</option>
          @for (c of cosifs; track c.codigo) {
            <option [value]="c.codigo">{{ c.codigo }} - ({{ c.classificacao || '—' }})</option>
          }
        </select>
      </div>
      <div>
        <label for="valor">Valor (R$)</label
        ><input
          id="valor"
          formControlName="valor"
          inputmode="decimal"
          placeholder="150,00"
          aria-describedby="valor-help"
        /><small id="valor-help">Até duas casas decimais, sem separador de milhar.</small>
      </div>
      <div>
        <label for="descricao">Descrição</label
        ><input
          id="descricao"
          formControlName="descricao"
          maxlength="50"
          placeholder="Identifique o lançamento"
        />
      </div>
    </div>
    @if (invalid) {
      <p class="alert error" role="alert">
        Preencha os campos: mês 1–12, ano 1–9999, produto, Cosif, descrição e valor com até duas
        casas decimais.
      </p>
    }
    <div class="form-actions">
      <span class="quiet">{{
        editing
          ? 'Preencha os dados do novo lançamento.'
          : 'Clique em Novo para iniciar um lançamento.'
      }}</span>
      <div>
        <button type="button" class="secondary" (click)="limpar()" [disabled]="!editing || saving">
          Limpar
        </button>
        <button type="button" class="secondary" (click)="startNew()" [disabled]="saving">
          ＋ Novo
        </button>
        <button type="submit" class="primary" [disabled]="!editing || saving || loadingCosifs">
          {{ saving ? 'Incluindo…' : 'Incluir movimento' }}
        </button>
      </div>
    </div>
  </form>`,
})
export class MovimentoForm implements OnChanges {
  private readonly fb = inject(FormBuilder);
  @Input() editing = false;
  @Input() saving = false;
  @Input() loadingCosifs = false;
  @Input() competencia: Competencia = { mes: 1, ano: 2026 };
  @Input() produtos: Produto[] = [];
  @Input() cosifs: Cosif[] = [];
  @Output() readonly incluir = new EventEmitter<NovoMovimento>();
  @Output() readonly novo = new EventEmitter<void>();
  @Output() readonly produtoChange = new EventEmitter<string>();
  invalid = false;
  readonly form = this.fb.nonNullable.group({
    mes: [
      1,
      [Validators.required, Validators.min(1), Validators.max(12), Validators.pattern(/^[0-9]+$/)],
    ],
    ano: [
      2026,
      [
        Validators.required,
        Validators.min(1),
        Validators.max(9999),
        Validators.pattern(/^[0-9]+$/),
      ],
    ],
    codigoProduto: ['', Validators.required],
    codigoCosif: ['', Validators.required],
    descricao: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(/.*\S.*/)]],
    valor: ['', [Validators.required, Validators.pattern(/^-?[0-9]{1,16}([,.][0-9]{1,2})?$/)]],
  });
  constructor() {
    this.form.disable({ emitEvent: false });
  }
  startNew() {
    if (!this.saving) {
      this.limpar();
      this.novo.emit();
    }
  }
  ngOnChanges(changes: import('@angular/core').SimpleChanges) {
    if (changes['editing']?.currentValue) this.limpar();
    if (this.editing && !this.saving) this.form.enable({ emitEvent: false });
    else this.form.disable({ emitEvent: false });
  }
  limpar() {
    this.invalid = false;
    this.form.reset({
      mes: this.competencia.mes,
      ano: this.competencia.ano,
      codigoProduto: '',
      codigoCosif: '',
      descricao: '',
      valor: '',
    });
    this.produtoChange.emit('');
  }
  changeProduto() {
    this.form.controls.codigoCosif.setValue('');
    this.produtoChange.emit(this.form.controls.codigoProduto.value);
  }
  submit() {
    if (!this.editing || this.saving || this.loadingCosifs) return;
    if (this.form.invalid) {
      this.invalid = true;
      this.form.markAllAsTouched();
      return;
    }
    this.invalid = false;
    const value = this.form.getRawValue();
    this.incluir.emit({
      ...value,
      descricao: value.descricao.trim(),
      valor: value.valor.replace(',', '.'),
    });
  }
}
