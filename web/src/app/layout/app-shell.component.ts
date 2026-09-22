import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterOutlet } from '@angular/router';
import { TenantSessionService } from '../core/auth/tenant-session.service';
import { AuthService } from '../core/auth/auth.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatButtonModule, MatIconModule, MatToolbarModule, RouterLink, RouterOutlet],
  selector: 'app-shell',
  template: `
    <mat-toolbar class="topbar">
      <a class="brand" routerLink="/dashboard" aria-label="Kine-plan, tableau de bord">
        <span class="brand-mark" aria-hidden="true">K</span>
        <span>Kine-plan</span>
      </a>
      <span class="toolbar-spacer"></span>
      <button mat-stroked-button type="button" aria-label="Cabinet actif">
        <mat-icon aria-hidden="true">business</mat-icon>
        {{ session.activeCabinet()?.name || 'Cabinet actif' }}
      </button>
      <button mat-icon-button type="button" aria-label="Se déconnecter" (click)="logout()">
        <mat-icon aria-hidden="true">account_circle</mat-icon>
      </button>
    </mat-toolbar>

    <div class="app-frame">
      <nav class="side-nav" aria-label="Navigation principale">
        <a routerLink="/dashboard" routerLinkActive="active"><mat-icon aria-hidden="true">dashboard</mat-icon>Tableau de bord</a>
        <a routerLink="/cabinet" routerLinkActive="active"><mat-icon aria-hidden="true">business</mat-icon>Cabinet</a>
        <a routerLink="/patients" routerLinkActive="active"><mat-icon aria-hidden="true">people</mat-icon>Patients</a>
        <a routerLink="/planning" routerLinkActive="active"><mat-icon aria-hidden="true">calendar_month</mat-icon>Planning</a>
        <a routerLink="/liste-attente" routerLinkActive="active"><mat-icon aria-hidden="true">hourglass_top</mat-icon>Liste d’attente</a>
        @if (session.role() === 'ADMIN') { <a routerLink="/administration" routerLinkActive="active"><mat-icon aria-hidden="true">manage_accounts</mat-icon>Administration</a> }
      </nav>
      <main class="page-content" tabindex="-1"><router-outlet /></main>
    </div>
  `,
  styles: `
    :host { display: block; min-height: 100dvh; background: #f4f7f7; }
    .topbar { background: #173b3d; color: #f6fbfa; min-height: 72px; padding: 0 28px; gap: 16px; }
    .brand { display: inline-flex; align-items: center; gap: 10px; color: inherit; text-decoration: none; font-weight: 800; letter-spacing: .02em; }
    .brand-mark { display: grid; place-items: center; width: 34px; height: 34px; background: #e67e22; color: #173b3d; border-radius: 9px; font-weight: 900; }
    .toolbar-spacer { flex: 1; }
    .app-frame { display: flex; min-height: calc(100dvh - 72px); }
    .side-nav { width: 244px; padding: 28px 16px; border-right: 1px solid #d9e5e3; background: #fff; }
    .side-nav a { display: flex; align-items: center; gap: 12px; padding: 13px 14px; margin-bottom: 5px; border-radius: 8px; color: #426366; text-decoration: none; font-size: .92rem; }
    .side-nav a:hover, .side-nav a.active { background: #e8f3f2; color: #0b7375; }
    .page-content { flex: 1; min-width: 0; padding: clamp(20px, 4vw, 48px); }
    @media (max-width: 720px) { .side-nav { width: 68px; padding-inline: 8px; } .side-nav a { justify-content: center; padding: 13px 8px; font-size: 0; } .side-nav mat-icon { margin: 0; } .topbar { padding-inline: 16px; } }
  `,
})
export class AppShellComponent {
  protected readonly session = inject(TenantSessionService);
  private readonly auth = inject(AuthService);

  logout(): void { this.auth.logout(); }
}
