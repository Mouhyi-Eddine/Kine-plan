import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TenantSessionService } from './tenant-session.service';
import { MembershipRole } from './auth.models';

export const authenticatedGuard: CanActivateFn = () => {
  const session = inject(TenantSessionService);
  return session.isAuthenticated() ? true : inject(Router).createUrlTree(['/auth/login']);
};

export const cabinetGuard: CanActivateFn = () => {
  const session = inject(TenantSessionService);
  return session.activeCabinet() ? true : inject(Router).createUrlTree(['/auth/select-cabinet']);
};

export const roleGuard = (roles: MembershipRole[]): CanActivateFn => () => {
  const session = inject(TenantSessionService);
  return session.role() && roles.includes(session.role()!) ? true : inject(Router).createUrlTree(['/dashboard']);
};