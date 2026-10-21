import { Component, EventEmitter, Input, Output, inject, DestroyRef, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged, filter } from 'rxjs';
import { Competencia } from '../models/movimento';
import { MESES, ANOS } from '../models/competencia-opcoes';
@Component({
  selector: 'app-competencia-filter',
  imports: [ReactiveFormsModule],
  template: ` <form [formGroup]="form" class="filter-form" (ngSubmit)="emit()">
    <label for="filtroMes"
      >Mês<select id="filtroMes" formControlName="mes">
        @for (mes of meses; track mes) {
          <option [ngValue]="mes">{{ mes.toString().padStart(2, '0') }}</option>
        }
      </select></label
    >
    <label for="filtroAno"
      >Ano<select id="filtroAno" formControlName="ano">
        @for (ano of anos; track ano) {
          <option [ngValue]="ano">{{ ano }}</option>
        }
      </select></label
    >
    <button type="submit" class="secondary" [disabled]="form.invalid">Atualizar</button>
  </form>`,
})
export class CompetenciaFilter implements OnInit {
  readonly meses = MESES;
  readonly anos = ANOS;
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  @Input({ required: true }) set competencia(value: Competencia) {
    this.form.setValue(value, { emitEvent: false });
  }
  @Output() readonly consultar = new EventEmitter<Competencia>();
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
  });
  ngOnInit() {
    this.form.valueChanges
      .pipe(
        debounceTime(250),
        filter(() => this.form.valid),
        distinctUntilChanged((a, b) => a.mes === b.mes && a.ano === b.ano),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.emit());
  }
  emit() {
    if (this.form.valid) this.consultar.emit(this.form.getRawValue());
  }
}
