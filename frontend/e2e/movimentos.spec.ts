import { test, expect } from '@playwright/test';
async function login(page: import('@playwright/test').Page) {
  await page.goto('/movimentos');
  await expect(page).toHaveURL(/login/);
  await page.getByLabel('Usuário', { exact: true }).fill('admin');
  await page.getByLabel('Senha', { exact: true }).fill('E2e-only-credential-2026');
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page.getByRole('heading', { name: 'Movimentos manuais' })).toBeVisible();
}
test('login, inclusão, consulta e logout com API/H2 reais', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', (error) => errors.push(error.message));
  await login(page);
  await expect(page.getByLabel('Descrição', { exact: true })).toBeDisabled();
  await page.getByRole('button', { name: 'Novo', exact: false }).click();
  await page.locator('#mes').fill('2');
  await page.locator('#ano').fill('2019');
  await page.getByLabel('Produto', { exact: true }).selectOption('P001');
  await page.getByLabel('Cosif', { exact: true }).selectOption('11111111111');
  await page.getByLabel('Valor (R$)').fill('9999999999999999,99');
  await page.getByLabel('Descrição', { exact: true }).fill('Movimento ponta a ponta');
  await page.getByRole('button', { name: 'Incluir movimento' }).click();
  await expect(page.getByRole('status')).toContainText('incluído com sucesso');
  await expect(page.getByLabel('Descrição', { exact: true })).toBeDisabled();
  await expect(page.getByRole('cell', { name: 'Movimento ponta a ponta' })).toBeVisible();
  await expect(page.getByRole('cell', { name: 'R$ 9.999.999.999.999.999,99' })).toBeVisible();
  await page.screenshot({ path: 'test-results/movimento-desktop.png', fullPage: true });
  await page.locator('#filtroMes').fill('3');
  await expect(page.getByRole('heading', { name: 'Nenhum movimento por aqui' })).toBeVisible();
  await page.locator('#filtroMes').fill('2');
  await expect(page.getByRole('cell', { name: 'Movimento ponta a ponta' })).toBeVisible();
  await page.getByRole('button', { name: 'Sair' }).click();
  await expect(page).toHaveURL(/login/);
  expect(errors).toEqual([]);
});
test('troca de produto, Limpar e validação', async ({ page }) => {
  await login(page);
  await page.getByRole('button', { name: 'Novo', exact: false }).click();
  await page.getByLabel('Produto', { exact: true }).selectOption('P001');
  await page.getByLabel('Cosif', { exact: true }).selectOption('11111111111');
  await page.getByLabel('Produto', { exact: true }).selectOption('P002');
  await expect(page.getByLabel('Cosif', { exact: true })).toHaveValue('');
  await expect(
    page
      .getByLabel('Cosif', { exact: true })
      .getByRole('option', { name: '22222222222 - (000003)' }),
  ).toBeAttached();
  await page.getByRole('button', { name: 'Incluir movimento' }).click();
  await expect(page.getByRole('alert')).toContainText('Preencha');
  await page.getByRole('button', { name: 'Limpar', exact: true }).click();
  await expect(page.getByLabel('Descrição', { exact: true })).toBeEnabled();
});
test('falha na consulta após salvar não reenvia o movimento', async ({ page }) => {
  await login(page);
  await page.getByRole('button', { name: 'Novo', exact: false }).click();
  await page.getByLabel('Produto', { exact: true }).selectOption('P002');
  await page.getByLabel('Cosif', { exact: true }).selectOption('22222222222');
  await page.getByLabel('Descrição', { exact: true }).fill('Salvar sem reenviar');
  await page.getByLabel('Valor (R$)').fill('10,01');
  await page.route('**/api/v1/movimentos?*', (route) =>
    route.fulfill({
      status: 503,
      contentType: 'application/problem+json',
      body: '{"detail":"Consulta indisponível."}',
    }),
  );
  await page.getByRole('button', { name: 'Incluir movimento' }).click();
  await expect(page.getByRole('status')).toContainText('incluído');
  await expect(page.getByRole('alert')).toContainText('Consulta indisponível');
  await expect(page.getByRole('button', { name: 'Incluir movimento' })).toBeDisabled();
});
test('layout móvel e sessão expirada', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await login(page);
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(390);
  await page.screenshot({ path: 'test-results/movimento-mobile.png', fullPage: true });
  await page.route('**/api/v1/movimentos?*', (route) =>
    route.fulfill({
      status: 401,
      contentType: 'application/problem+json',
      body: '{"detail":"Token expirado"}',
    }),
  );
  await page.getByRole('button', { name: 'Atualizar', exact: true }).click();
  await expect(page).toHaveURL(/login/);
});
