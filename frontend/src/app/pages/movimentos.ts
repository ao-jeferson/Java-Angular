import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, catchError, finalize, of, switchMap, tap } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { apiError } from '../auth/api-error';
import { MovimentosApi } from '../services/movimentos-api.service';
import { ProdutosApi } from '../services/produtos-api.service';
import { Competencia, Cosif, Movimento, NovoMovimento, Produto } from '../models/movimento';
import { MovimentoForm } from '../components/movimento-form';
import { MovimentosGrid } from '../components/movimentos-grid';
import { CompetenciaFilter } from '../components/competencia-filter';
@Component({
  selector: 'app-movimentos-page',
  imports: [MovimentoForm, MovimentosGrid, CompetenciaFilter],
  template: ` <header class="topbar">
      <a class="wordmark" href="/">M<span>↗</span> Movimento</a>
      <div class="topbar-actions">
        <span class="session-badge">● Sessão protegida</span
        ><button class="text-button" (click)="auth.logout()">Sair ↗</button>
      </div>
    </header>
    <main class="workspace">
      <div class="page-heading">
        <div>
          <p class="eyebrow">GESTÃO DE LANÇAMENTOS</p>
          <h1>Movimentos manuais<span class="accent">.</span></h1>
          <p class="muted">Registre, consulte e acompanhe os movimentos de cada competência.</p>
        </div>
        <span class="environment-tag">CONTROLE FINANCEIRO</span>
      </div>
      @if (notice()) {
        <p class="alert success" role="status">{{ notice() }}</p>
      }
      @if (error()) {
        <p class="alert error" role="alert">{{ error() }}</p>
      }
      <section class="panel">
        <div class="panel-heading">
          <div>
            <span class="step">01</span>
            <h2>Novo lançamento</h2>
          </div>
          <span class="status-chip">{{ editing() ? 'Em edição' : 'Pronto para iniciar' }}</span>
        </div>
        <app-movimento-form
          [editing]="editing()"
          [saving]="saving()"
          [loadingCosifs]="loadingCosifs()"
          [competencia]="competencia()"
          [produtos]="produtos()"
          [cosifs]="cosifs()"
          (novo)="novo()"
          (produtoChange)="produtoChanges.next($event)"
          (incluir)="incluir($event)"
        />
      </section>
      <section class="panel grid-panel">
        <div class="panel-heading grid-heading">
          <div>
            <span class="step">02</span>
            <h2>Movimentos da competência</h2>
            <span class="count">{{ movimentos().length }}</span>
          </div>
          <app-competencia-filter [competencia]="competencia()" (consultar)="consultar($event)" />
        </div>
        @if (loading()) {
          <div class="empty-state" role="status">Consultando movimentos…</div>
        } @else if (gridError()) {
          <div class="empty-state">
            <p class="error-text" role="alert">{{ gridError() }}</p>
            <button class="secondary" (click)="consultar(competencia())">Tentar novamente</button>
          </div>
        } @else if (movimentos().length === 0) {
          <div class="empty-state">
            <span class="empty-icon">↔</span>
            <h3>Nenhum movimento por aqui</h3>
            <p>Selecione outra competência ou crie o primeiro lançamento.</p>
          </div>
        } @else {
          <app-movimentos-grid [movimentos]="movimentos()" />
        }
        <div class="panel-footer">
          <span
            >Competência {{ competencia().mes.toString().padStart(2, '0') }} /
            {{ competencia().ano }}</span
          ><span>Dados consultados diretamente na base</span>
        </div>
      </section>
      <footer class="workspace-footer">
        <span>Movimento · Precisão em cada lançamento.</span><span>Java + Angular</span>
      </footer>
    </main>`,
})
export class MovimentosPage implements OnInit {
  readonly auth = inject(AuthService);
  private readonly api = inject(MovimentosApi);
  private readonly catalogo = inject(ProdutosApi);
  private readonly destroyRef = inject(DestroyRef);
  readonly competencia = signal<Competencia>({
    mes: new Date().getMonth() + 1,
    ano: new Date().getFullYear(),
  });
  readonly movimentos = signal<Movimento[]>([]);
  readonly produtos = signal<Produto[]>([]);
  readonly cosifs = signal<Cosif[]>([]);
  readonly editing = signal(false);
  readonly saving = signal(false);
  readonly loading = signal(false);
  readonly loadingCosifs = signal(false);
  readonly error = signal('');
  readonly gridError = signal('');
  readonly notice = signal('');
  readonly produtoChanges = new Subject<string>();
  private readonly consultas = new Subject<Competencia>();
  constructor() {
    this.consultas
      .pipe(
        switchMap((c) => {
          this.loading.set(true);
          this.gridError.set('');
          this.movimentos.set([]);
          return this.api.consultar(c).pipe(
            catchError((error) => {
              this.gridError.set(apiError(error));
              return of([]);
            }),
            finalize(() => this.loading.set(false)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((rows) => this.movimentos.set(rows));
    this.produtoChanges
      .pipe(
        switchMap((codigo) => {
          this.cosifs.set([]);
          this.loadingCosifs.set(!!codigo);
          return (codigo ? this.catalogo.cosifs(codigo) : of([])).pipe(
            catchError((error) => {
              this.error.set(apiError(error));
              return of([]);
            }),
            finalize(() => this.loadingCosifs.set(false)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((rows) => this.cosifs.set(rows));
  }
  ngOnInit() {
    this.carregarProdutos();
    this.consultar(this.competencia());
  }
  private carregarProdutos() {
    this.catalogo
      .produtos()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (rows) => this.produtos.set(rows),
        error: (error) => this.error.set(apiError(error)),
      });
  }
  consultar(c: Competencia) {
    this.competencia.set(c);
    this.consultas.next(c);
  }
  novo() {
    if (this.saving()) return;
    this.error.set('');
    this.notice.set('');
    if (!this.produtos().length) this.carregarProdutos();
    this.editing.set(true);
  }
  incluir(comando: NovoMovimento) {
    if (this.saving()) return;
    this.saving.set(true);
    this.error.set('');
    this.notice.set('');
    this.api
      .incluir(comando)
      .pipe(
        finalize(() => this.saving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (m) => {
          this.editing.set(false);
          this.notice.set(`Movimento ${m.numeroLancamento} incluído com sucesso.`);
          this.consultar({ mes: m.mes, ano: m.ano });
        },
        error: (error) => this.error.set(apiError(error)),
      });
  }
}
