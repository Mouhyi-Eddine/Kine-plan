import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CabinetAccess, MembershipRole, TokenResponse, UserProfile } from './auth.models';

const ACCESS_TOKEN_KEY = 'kineplan.access-token';
const REFRESH_TOKEN_KEY = 'kineplan.refresh-token';
const CABINETS_KEY = 'kineplan.cabinets';
const ACTIVE_CABINET_KEY = 'kineplan.active-cabinet';

@Injectable({ providedIn: 'root' })
export class TenantSessionService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly browser = isPlatformBrowser(this.platformId);
  private readonly accessTokenState = signal(this.read(ACCESS_TOKEN_KEY));
  private readonly refreshTokenState = signal(this.read(REFRESH_TOKEN_KEY));
  private readonly cabinetsState = signal<CabinetAccess[]>(this.readJson<CabinetAccess[]>(CABINETS_KEY) ?? []);
  private readonly activeCabinetState = signal<CabinetAccess | null>(this.readJson<CabinetAccess>(ACTIVE_CABINET_KEY));
  private readonly profileState = signal<UserProfile | null>(null);

  readonly accessToken = this.accessTokenState.asReadonly();
  readonly refreshToken = this.refreshTokenState.asReadonly();
  readonly cabinets = this.cabinetsState.asReadonly();
  readonly activeCabinet = this.activeCabinetState.asReadonly();
  readonly profile = this.profileState.asReadonly();
  readonly role = computed<MembershipRole | null>(() => this.activeCabinetState()?.role ?? null);
  readonly isAuthenticated = computed(() => Boolean(this.accessTokenState()));
  readonly hasPreAuth = computed(() => Boolean(this.read('kineplan.pre-auth')));

  preAuthToken(): string | null { return this.read('kineplan.pre-auth'); }

  setPreAuthToken(token: string): void {
    this.write('kineplan.pre-auth', token);
  }

  setCabinets(cabinets: CabinetAccess[]): void {
    this.cabinetsState.set(cabinets);
    this.writeJson(CABINETS_KEY, cabinets);
  }

  setTokens(tokens: TokenResponse): void {
    this.accessTokenState.set(tokens.accessToken);
    this.refreshTokenState.set(tokens.refreshToken);
    this.write(ACCESS_TOKEN_KEY, tokens.accessToken);
    this.write(REFRESH_TOKEN_KEY, tokens.refreshToken);
    this.remove('kineplan.pre-auth');
  }

  setActiveCabinet(cabinet: CabinetAccess): void {
    this.activeCabinetState.set(cabinet);
    this.writeJson(ACTIVE_CABINET_KEY, cabinet);
  }

  setProfile(profile: UserProfile): void {
    this.profileState.set(profile);
    const cabinet = this.cabinetsState().find((item) => item.id === profile.cabinetId);
    if (cabinet) this.setActiveCabinet(cabinet);
  }

  clear(): void {
    this.accessTokenState.set(null);
    this.refreshTokenState.set(null);
    this.cabinetsState.set([]);
    this.activeCabinetState.set(null);
    this.profileState.set(null);
    [ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY, CABINETS_KEY, ACTIVE_CABINET_KEY, 'kineplan.pre-auth'].forEach((key) => this.remove(key));
  }

  private read(key: string): string | null {
    return this.browser ? localStorage.getItem(key) : null;
  }

  private readJson<T>(key: string): T | null {
    const value = this.read(key);
    if (!value) return null;
    try { return JSON.parse(value) as T; } catch { return null; }
  }

  private write(key: string, value: string): void { if (this.browser) localStorage.setItem(key, value); }
  private writeJson(key: string, value: unknown): void { this.write(key, JSON.stringify(value)); }
  private remove(key: string): void { if (this.browser) localStorage.removeItem(key); }
}