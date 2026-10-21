import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors, HttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { authInterceptor } from './auth.interceptor';
import { authGuard } from './auth.guard';
describe('Autenticação e proteção de rotas', () => {
  let http: HttpTestingController;
  let auth: AuthService;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
  });
  afterEach(() => {
    auth.clear();
    http.verify();
  });
  function login() {
    auth.login('admin', 'senha').subscribe();
    http
      .expectOne('/api/v1/auth/login')
      .flush({ accessToken: 'jwt-test', expiresIn: 900, tokenType: 'Bearer' });
  }
  it('redireciona visitante para login', () => {
    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    expect(result.toString()).toBe('/login');
  });
  it('permite usuário autenticado e envia bearer apenas à API local', () => {
    login();
    expect(auth.authenticated()).toBe(true);
    expect(TestBed.runInInjectionContext(() => authGuard({} as never, {} as never))).toBe(true);
    const client = TestBed.inject(HttpClient);
    client.get('/api/v1/produtos').subscribe();
    expect(http.expectOne('/api/v1/produtos').request.headers.get('Authorization')).toBe(
      'Bearer jwt-test',
    );
    client.get('https://example.com/api/v1/produtos').subscribe();
    expect(
      http.expectOne('https://example.com/api/v1/produtos').request.headers.has('Authorization'),
    ).toBe(false);
  });
  it('limpa sessão e redireciona ao receber 401', () => {
    login();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    TestBed.inject(HttpClient)
      .get('/api/v1/produtos')
      .subscribe({ error: () => {} });
    http.expectOne('/api/v1/produtos').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(auth.token()).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login']);
  });
  it('expira o token automaticamente', () => {
    vi.useFakeTimers();
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    login();
    vi.advanceTimersByTime(900000);
    expect(auth.authenticated()).toBe(false);
    vi.useRealTimers();
  });
});
