import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/auth/auth.service';
import { TenantSessionService } from '../../core/auth/tenant-session.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, ReactiveFormsModule, RouterLink],
  selector: 'app-login',
  template: `
    <main class="auth-page">
      <section class="auth-panel" aria-labelledby="login-title">
        <div class="brand-lockup"><span class="brand-mark" aria-hidden="true">K</span><span>Kine-plan</span></div>
        <p class="eyebrow">ESPACE CABINET</p>
        <h1 id="login-title">Retrouver votre rythme de soin.</h1>
        <p class="intro">Connectez-vous pour retrouver vos patients, votre planning et votre cabinet actif.</p>
        <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
          <mat-form-field appearance="outline">
            <mat-label>Adresse email</mat-label>
            <input matInput type="email" formControlName="email" autocomplete="username" />
            @if (form.controls.email.invalid && form.controls.email.touched) { <mat-error>Entrez une adresse email valide.</mat-error> }
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Mot de passe</mat-label>
            <input matInput type="password" formControlName="password" autocomplete="current-password" />
            @if (form.controls.password.invalid && form.controls.password.touched) { <mat-error>Le mot de passe est requis.</mat-error> }
          </mat-form-field>
          @if (errorMessage()) { <p class="error" role="alert">{{ errorMessage() }}</p> }
          <button mat-flat-button class="submit" type="submit" [disabled]="form.invalid || loading()">
            {{ loading() ? 'Connexion...' : 'Se connecter' }}
          </button>
        </form>
        <a routerLink="/onboarding" class="secondary-link">Créer un nouveau cabinet</a>
      </section>
      <aside class="auth-aside" aria-label="Présentation de Kine-plan">
        <span class="aside-index">01 / CABINET</span>
        <h2>Le soin au centre.<br /><em>Le reste en rythme.</em></h2>
        <p>Un espace clair pour coordonner les journées, les patients et les équipes qui les accompagnent.</p>
      </aside>
    </main>
  `,
  styles: `
    :host { display: block; min-height: 100dvh; background: #f4f7f7; }
    .auth-page { display: grid; grid-template-columns: minmax(0, 1.05fr) minmax(360px, .95fr); min-height: 100dvh; }
    .auth-panel { width: min(480px, calc(100% - 48px)); margin: auto; padding: 40px 0; }
    .brand-lockup { display: flex; align-items: center; gap: 10px; color: #173b3d; font-weight: 800; font-size: 1.1rem; }
    .brand-mark { display: grid; place-items: center; width: 36px; height: 36px; border-radius: 9px; background: #e67e22; color: #173b3d; font-weight: 900; }
    .eyebrow, .aside-index { color: #0b7375; font-size: .72rem; font-weight: 800; letter-spacing: .16em; margin: 72px 0 16px; }
    h1 { max-width: 480px; margin: 0; color: #173b3d; font-size: clamp(2.3rem, 5vw, 4.5rem); line-height: .98; letter-spacing: 0; }
    .intro { max-width: 410px; color: #587174; line-height: 1.7; margin: 24px 0 36px; }
    form { display: grid; gap: 12px; }
    .submit { min-height: 50px; background: #0b7375; color: #fff; }
    .secondary-link { display: inline-block; margin-top: 24px; color: #0b7375; font-weight: 700; }
    .error { color: #a33c2f; margin: 0; font-size: .9rem; }
    .auth-aside { display: flex; flex-direction: column; justify-content: flex-end; padding: 64px; min-height: 100dvh; background: #173b3d; color: #f6fbfa; }
    .auth-aside .aside-index { color: #e67e22; margin: 0 0 auto; }
    .auth-aside h2 { font-size: clamp(2.4rem, 5vw, 5.4rem); line-height: .95; margin: 0 0 26px; }
    .auth-aside em { color: #e67e22; font-style: normal; }
    .auth-aside p { max-width: 340px; color: #b9d0ce; line-height: 1.7; }
    @media (max-width: 760px) { .auth-page { display: block; } .auth-panel { padding-top: 48px; } .auth-aside { min-height: 260px; padding: 32px 24px; } .auth-aside .aside-index { margin: 0 0 40px; } .auth-aside h2 { font-size: 2.5rem; } }
  `,
})
export class LoginComponent {
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly session = inject(TenantSessionService);
  private readonly router = inject(Router);
  protected readonly form = this.formBuilder.group({ email: ['', [Validators.required, Validators.email]], password: ['', Validators.required] });
  protected readonly loading = signal(false);
  protected readonly errorMessage = signal('');

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true); this.errorMessage.set('');
    this.auth.login(this.form.getRawValue()).subscribe({
      next: (response) => {
        this.loading.set(false);
        if (response.cabinets.length === 1) {
          this.session.setActiveCabinet(response.cabinets[0]);
          this.auth.selectCabinet({ cabinetId: response.cabinets[0].id }).subscribe({ next: () => void this.router.navigateByUrl('/dashboard'), error: () => this.fail() });
        } else { void this.router.navigateByUrl('/auth/select-cabinet'); }
      }, error: () => this.fail(),
    });
  }

  private fail(): void { this.loading.set(false); this.errorMessage.set('Connexion impossible. Vérifiez vos identifiants ou réessayez plus tard.'); }
}