import { TestBed } from '@angular/core/testing';
import { MovimentoForm } from './movimento-form';
describe('Formulário de movimento', () => {
  it('começa desabilitado e habilita ao clicar Novo', async () => {
    const fixture = TestBed.createComponent(MovimentoForm);
    fixture.detectChanges();
    expect(fixture.componentInstance.form.disabled).toBe(true);
    fixture.componentRef.setInput('editing', true);
    fixture.detectChanges();
    expect(fixture.componentInstance.form.enabled).toBe(true);
  });
  it('valida campos e converte valor sem perder precisão', () => {
    const fixture = TestBed.createComponent(MovimentoForm);
    fixture.componentRef.setInput('editing', true);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    const emit = vi.spyOn(component.incluir, 'emit');
    component.submit();
    expect(emit).not.toHaveBeenCalled();
    component.form.setValue({
      mes: 2,
      ano: 2019,
      codigoProduto: 'P001',
      codigoCosif: '11111111111',
      descricao: 'Teste',
      valor: '9999999999999999,99',
    });
    component.submit();
    expect(emit).toHaveBeenCalledWith(expect.objectContaining({ valor: '9999999999999999.99' }));
  });
  it('trocar produto limpa Cosif e Limpar mantém edição', () => {
    const fixture = TestBed.createComponent(MovimentoForm);
    fixture.componentRef.setInput('editing', true);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.form.controls.codigoCosif.setValue('11111111111');
    component.changeProduto();
    expect(component.form.controls.codigoCosif.value).toBe('');
    component.limpar();
    expect(component.editing).toBe(true);
    expect(component.form.enabled).toBe(true);
  });
});
