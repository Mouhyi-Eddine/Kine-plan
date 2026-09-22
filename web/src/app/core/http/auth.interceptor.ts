import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { TenantSessionService } from '../auth/tenant-session.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const session = inject(TenantSessionService);
  const auth = inject(AuthService);
  const token = session.accessToken();
  const preAuthToken = session.preAuthToken();
  const isSelection = request.url.endsWith('/auth/select-cabinet');
  const requestToken = isSelection ? preAuthToken : token;
  const authenticatedRequest = requestToken && !request.url.endsWith('/auth/login') && !request.url.endsWith('/auth/refresh')
    ? request.clone({ setHeaders: { Authorization: `Bearer ${requestToken}` } })
    : request;

  return next(authenticatedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401 || !session.refreshToken() || request.url.endsWith('/auth/refresh')) {
        return throwError(() => error);
      }
      return auth.refresh().pipe(
        switchMap(() => next(request.clone({ setHeaders: { Authorization: `Bearer ${session.accessToken()}` } }))),
        catchError((refreshError) => {
          session.clear();
          return throwError(() => refreshError);
        }),
      );
    }),
  );
};