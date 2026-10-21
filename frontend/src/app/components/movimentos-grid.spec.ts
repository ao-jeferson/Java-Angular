import { money } from './movimentos-grid';
describe('Formatação monetária', () => {
  it('preserva todos os dígitos de NUMERIC(18,2)', () =>
    expect(money('9999999999999999.99')).toBe('R$ 9.999.999.999.999.999,99'));
  it('formata zero e valores negativos', () => {
    expect(money('0.00')).toBe('R$ 0,00');
    expect(money('-1500.05')).toBe('R$ -1.500,05');
  });
});
