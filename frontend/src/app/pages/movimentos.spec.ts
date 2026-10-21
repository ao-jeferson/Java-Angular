import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { of, Subject, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { MovimentosPage } from './movimentos';
import { MovimentosApi } from '../services/movimentos-api.service';
import { ProdutosApi } from '../services/produtos-api.service';
import { Movimento, Cosif } from '../models/movimento';
describe('Página de movimentos', () => {
  const movimento: Movimento = {
    mes: 2,
    ano: 2019,
    codigoProduto: 'P001',
    codigoCosif: '11111111111',
    descricao: 'Teste',
    valor: '150.00',
    numeroLancamento: '1',
    descricaoProduto: 'Renda fixa',
    dataMovimento: '2026-10-20T20:00:00',
    codigoUsuario: 'TESTE',
  };
  function setup() {
    const api = {
      consultar: vi.fn().mockReturnValue(of([])),
      incluir: vi.fn().mockReturnValue(of(movimento)),
    };
    const catalogo = { produtos: vi.fn().mockReturnValue(of([])), cosifs: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(),
        { provide: MovimentosApi, useValue: api },
        { provide: ProdutosApi, useValue: catalogo },
      ],
    });
    const fixture = TestBed.createComponent(MovimentosPage);
    fixture.detectChanges();
    return { component: fixture.componentInstance, api, catalogo };
  }
  it('consulta automaticamente e após inclusão retorna ao estado desabilitado', () => {
    const { component, api } = setup();
    expect(api.consultar).toHaveBeenCalledTimes(1);
    component.novo();
    component.incluir(movimento);
    expect(component.editing()).toBe(false);
    expect(api.consultar).toHaveBeenLastCalledWith({ mes: 2, ano: 2019 });
    expect(component.notice()).toContain('incluído');
  });
  it('não reenvia inclusão se a consulta após gravar falhar', () => {
    const { component, api } = setup();
    api.consultar.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 503 })));
    component.novo();
    component.incluir(movimento);
    expect(component.notice()).toContain('incluído');
    expect(component.gridError()).not.toBe('');
    expect(api.incluir).toHaveBeenCalledTimes(1);
  });
  it('preserva edição quando inclusão falha', () => {
    const { component, api } = setup();
    api.incluir.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 422, error: { detail: 'Cosif inválido' } })),
    );
    component.novo();
    component.incluir(movimento);
    expect(component.editing()).toBe(true);
    expect(component.error()).toBe('Cosif inválido');
  });
  it('descarta resposta obsoleta de Cosif', () => {
    const { component, catalogo } = setup();
    const primeira = new Subject<Cosif[]>();
    const segunda = new Subject<Cosif[]>();
    catalogo.cosifs.mockReturnValueOnce(primeira).mockReturnValueOnce(segunda);
    component.produtoChanges.next('P001');
    component.produtoChanges.next('P002');
    segunda.next([{ codigo: '22222222222', classificacao: '000003' }]);
    primeira.next([{ codigo: '11111111111', classificacao: '000001' }]);
    expect(component.cosifs()[0].codigo).toBe('22222222222');
  });
});
