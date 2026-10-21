import { defineConfig, devices } from '@playwright/test';
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: 'http://127.0.0.1:14200',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: [
    {
      command:
        'java -jar ../backend/target/movimento-1.1.0.jar --server.port=18080 --movimento.cors.origins=http://127.0.0.1:14200 "--spring.datasource.url=jdbc:h2:mem:e2e;DB_CLOSE_DELAY=-1" --logging.level.root=WARN',
      url: 'http://127.0.0.1:18080/actuator/health',
      timeout: 90000,
      reuseExistingServer: false,
      env: { MOVIMENTO_PASSWORD: 'E2e-only-credential-2026' },
    },
    {
      command: 'npx ng serve --host 127.0.0.1 --port 14200 --proxy-config proxy.e2e.json',
      url: 'http://127.0.0.1:14200',
      timeout: 90000,
      reuseExistingServer: false,
    },
  ],
});
