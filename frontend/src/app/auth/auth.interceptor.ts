import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const protectedApi = request.url.startsWith('/api/v1/') && request.url !== '/api/v1/auth/login';
  if (protectedApi && auth.token())
    request = request.clone({ setHeaders: { Authorization: `Bearer ${auth.token()}` } });
  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (protectedApi && error.status === 401) auth.logout();
      return throwError(() => error);
    }),
  );
};
