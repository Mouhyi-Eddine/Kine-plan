import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/auth/auth.service';
import { TenantSessionService } from '../../core/auth/tenant-session.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatButtonModule, MatCardModule, MatIconModule],
  selector: 'app-select-cabinet',
  template: `
    <main class="selection-page"><section class="selection-panel" aria-labelledby="selection-title">
      <p class="eyebrow">CONTEXTE DE TRAVAIL</p><h1 id="selection-title">Quel cabinet ouvrez-vous aujourd’hui ?</h1>
      <p class="intro">Votre accès et votre agenda restent strictement séparés pour chaque cabinet.</p>
      <div class="cabinet-list">
        @for (cabinet of session.cabinets(); track cabinet.id) {
          <button class="cabinet-option" type="button" (click)="select(cabinet.id)" [disabled]="loading()">
            <span class="cabinet-icon"><mat-icon aria-hidden="true">business</mat-icon></span><span><strong>{{ cabinet.name }}</strong><small>{{ cabinet.role }}</small></span><mat-icon aria-hidden="true">arrow_forward</mat-icon>
          </button>
        }
      </div>
      @if (errorMessage()) { <p class="error" role="alert">{{ errorMessage() }}</p> }
    </section></main>
  `,
  styles: `
    :host { display: block; min-height: 100dvh; background: #f4f7f7; }
    .selection-page { display: grid; place-items: center; min-height: 100dvh; padding: 24px; background: radial-gradient(circle at 80% 20%, #d9efeb 0, transparent 30%), #f4f7f7; }
    .selection-panel { width: min(640px, 100%); padding: 48px; background: #fff; border: 1px solid #d9e5e3; border-radius: 12px; box-shadow: 0 20px 60px rgba(23,59,61,.08); }
    .eyebrow { color: #0b7375; font-size: .72rem; font-weight: 800; letter-spacing: .16em; } h1 { color: #173b3d; font-size: clamp(2rem, 5vw, 3.6rem); line-height: 1; margin: 0; } .intro { color: #587174; line-height: 1.6; margin-bottom: 32px; }
    .cabinet-list { display: grid; gap: 10px; } .cabinet-option { display: grid; grid-template-columns: auto 1fr auto; align-items: center; gap: 16px; width: 100%; padding: 16px; border: 1px solid #c9dcda; border-radius: 8px; background: #fff; color: #173b3d; text-align: left; cursor: pointer; } .cabinet-option:hover { border-color: #0b7375; background: #f1f9f7; } .cabinet-option small { display: block; margin-top: 4px; color: #71898a; text-transform: capitalize; } .cabinet-icon { display: grid; place-items: center; width: 42px; height: 42px; border-radius: 8px; background: #e8f3f2; color: #0b7375; } .error { color: #a33c2f; }
    @media (max-width: 560px) { .selection-panel { padding: 28px 20px; } }
  `,
})
export class SelectCabinetComponent {
  protected readonly session = inject(TenantSessionService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly loading = signal(false);
  protected readonly errorMessage = signal('');

  select(cabinetId: string): void {
    this.loading.set(true); this.errorMessage.set('');
    this.auth.selectCabinet({ cabinetId }).subscribe({ next: () => { const cabinet = this.session.cabinets().find((item) => item.id === cabinetId); if (cabinet) this.session.setActiveCabinet(cabinet); void this.router.navigateByUrl('/dashboard'); }, error: () => { this.loading.set(false); this.errorMessage.set('Ce cabinet n’est plus accessible.'); } });
  }
}