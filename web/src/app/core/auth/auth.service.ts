import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthApi } from './auth.api';
import { LoginRequest, LoginResponse, SelectCabinetRequest, TokenResponse } from './auth.models';
import { TenantSessionService } from './tenant-session.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(AuthApi);
  private readonly session = inject(TenantSessionService);
  private readonly router = inject(Router);

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.api.login(request).pipe(tap((response) => {
      this.session.setPreAuthToken(response.preAuthToken);
      this.session.setCabinets(response.cabinets);
    }));
  }

  selectCabinet(request: SelectCabinetRequest): Observable<TokenResponse> {
    return this.api.selectCabinet(request).pipe(tap((tokens) => this.session.setTokens(tokens)));
  }

  refresh(): Observable<TokenResponse> {
    const token = this.session.refreshToken();
    if (!token) throw new Error('Refresh token unavailable');
    return this.api.refresh(token).pipe(tap((tokens) => this.session.setTokens(tokens)));
  }

  logout(): void {
    const token = this.session.refreshToken();
    if (token) this.api.logout(token).subscribe({ error: () => undefined });
    this.session.clear();
    void this.router.navigateByUrl('/auth/login');
  }
}