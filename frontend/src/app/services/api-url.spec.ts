import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { API_BASE_URL } from './api-url';
import { authInterceptor } from '../auth/auth.interceptor';
import { AuthService } from '../auth/auth.service';

describe('API externa do site', () => {
  it('envia o JWT somente para a API configurada', () => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: API_BASE_URL, useValue: 'https://api.example.com/api/v1' },
        { provide: AuthService, useValue: { token: () => 'jwt-test', logout: vi.fn() } },
      ],
    });
    const client = TestBed.inject(HttpClient);
    const http = TestBed.inject(HttpTestingController);
    for (const url of [
      'https://api.example.com/api/v1/produtos',
      'https://api.example.com.evil.test/api/v1/produtos',
    ]) {
      client.get(url).subscribe();
      const request = http.expectOne(url);
      expect(request.request.headers.has('Authorization')).toBe(
        url.startsWith('https://api.example.com/'),
      );
      request.flush([]);
    }
    http.verify();
  });
});
