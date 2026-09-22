import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { HttpClient } from '@angular/common/http';
import { API_BASE_URL } from '../../core/api/api.config';

@Component({
  selector: 'app-onboarding',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule, ReactiveFormsModule, RouterLink],
  template: `
    <main class="page" aria-labelledby="onboarding-title">
      <a routerLink="/auth/login">← Retour à la connexion</a>
      <p class="eyebrow">NOUVEAU CABINET</p>
      <h1 id="onboarding-title">Créer votre espace de soin.</h1>
      <p class="intro">Le premier compte sera administrateur du cabinet.</p>
      <mat-card appearance="outlined"><form [formGroup]="form" (ngSubmit)="submit()" novalidate>
        <h2>Cabinet</h2>
        <mat-form-field appearance="outline"><mat-label>Nom du cabinet</mat-label><input matInput formControlName="cabinetName" /></mat-form-field>
        <mat-form-field appearance="outline"><mat-label>Adresse</mat-label><input matInput formControlName="address" /></mat-form-field>
        <mat-form-field appearance="outline"><mat-label>SIRET</mat-label><input matInput formControlName="siret" inputmode="numeric" /></mat-form-field>
        <mat-form-field appearance="outline"><mat-label>Fuseau horaire</mat-label><input matInput formControlName="timezone" /></mat-form-field>
        <h2>Compte administrateur</h2>
        <div class="row"><mat-form-field appearance="outline"><mat-label>Prénom</mat-label><input matInput formControlName="adminFirstName" /></mat-form-field><mat-form-field appearance="outline"><mat-label>Nom</mat-label><input matInput formControlName="adminLastName" /></mat-form-field></div>
        <mat-form-field appearance="outline"><mat-label>Email</mat-label><input matInput type="email" formControlName="adminEmail" autocomplete="email" /></mat-form-field>
        <mat-form-field appearance="outline"><mat-label>Mot de passe</mat-label><input matInput type="password" formControlName="adminPassword" autocomplete="new-password" /><mat-hint>12 caractères minimum</mat-hint></mat-form-field>
        @if (errorMessage()) { <p class="error" role="alert">Impossible de créer le cabinet. Vérifiez les informations saisies.</p> }
        <button mat-flat-button type="submit" [disabled]="form.invalid || saving()">{{ saving() ? 'Création...' : 'Créer le cabinet' }}</button>
      </form></mat-card>
    </main>
  `,
  styles: `.page{max-width:760px;margin:auto;padding:40px 24px}.page>a{color:#0b7375;text-decoration:none}.eyebrow{color:#0b7375;font-size:.72rem;font-weight:800;letter-spacing:.16em;margin:40px 0 14px}h1{color:#173b3d;font-size:clamp(2.4rem,5vw,4.2rem);line-height:.95;margin:0}.intro{color:#587174;margin:16px 0 28px}mat-card{padding:24px;border-color:#d9e5e3}form{display:grid;gap:14px}h2{color:#173b3d;font-size:1.1rem;margin:8px 0 0}.row{display:grid;grid-template-columns:1fr 1fr;gap:14px}button{background:#0b7375;color:#fff;min-height:46px}.error{color:#a33c2f}@media(max-width:640px){.row{grid-template-columns:1fr}}`,
})
export class OnboardingComponent {
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);
  private readonly router = inject(Router);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal('');
  readonly form = this.formBuilder.group({
    cabinetName: ['', [Validators.required, Validators.maxLength(200)]], address: ['', Validators.maxLength(500)],
    siret: ['', [Validators.pattern(/^$|^\d{14}$/)]], timezone: ['Europe/Paris', Validators.required],
    adminEmail: ['', [Validators.required, Validators.email]], adminPassword: ['', [Validators.required, Validators.minLength(12)]],
    adminFirstName: ['', Validators.required], adminLastName: ['', Validators.required],
  });

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true); this.errorMessage.set('');
    this.http.post(`${this.baseUrl}/onboarding`, this.form.getRawValue()).subscribe({
      next: () => void this.router.navigate(['/auth/login'], { queryParams: { onboarding: 'success' } }),
      error: () => { this.saving.set(false); this.errorMessage.set('error'); },
    });
  }
}
