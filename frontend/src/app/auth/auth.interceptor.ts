import { API_BASE_URL } from '../services/api-url';
import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const apiBase = inject(API_BASE_URL);
  const protectedApi =
    request.url.startsWith(`${apiBase}/`) && request.url !== `${apiBase}/auth/login`;
  if (protectedApi && auth.token())
    request = request.clone({ setHeaders: { Authorization: `Bearer ${auth.token()}` } });
  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (protectedApi && error.status === 401) auth.logout();
      return throwError(() => error);
    }),
  );
};
