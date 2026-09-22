import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { PatientsApi } from './patients.api';
import { PageResponse, PatientResponse } from './patients.models';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, ReactiveFormsModule, RouterLink],
  selector: 'app-patients-list',
  template: `
    <section class="patients-page" aria-labelledby="patients-title">
      <header class="page-header"><div><p class="eyebrow">DOSSIERS / CABINET ACTIF</p><h1 id="patients-title">Patients</h1><p class="subheading">Retrouvez rapidement les dossiers suivis par votre équipe.</p></div><button mat-flat-button class="primary-action" type="button"><mat-icon aria-hidden="true">person_add</mat-icon>Nouveau patient</button></header>
      <form class="search-row" [formGroup]="searchForm" (ngSubmit)="search()" role="search"><mat-form-field appearance="outline"><mat-label>Rechercher un patient</mat-label><input matInput formControlName="query" placeholder="Nom, prénom ou téléphone" /><mat-icon matPrefix aria-hidden="true">search</mat-icon></mat-form-field><button mat-stroked-button type="submit">Rechercher</button></form>
      @if (loading()) { <div class="state" role="status"><mat-icon aria-hidden="true">progress_activity</mat-icon>Chargement des dossiers...</div> }
      @if (errorMessage()) { <div class="state error" role="alert"><mat-icon aria-hidden="true">cloud_off</mat-icon>{{ errorMessage() }}</div> }
      @if (!loading() && !errorMessage() && patients().content.length === 0) { <mat-card appearance="outlined" class="empty"><mat-icon aria-hidden="true">folder_open</mat-icon><h2>Aucun patient trouvé</h2><p>Modifiez votre recherche ou créez un nouveau dossier.</p></mat-card> }
      @if (!loading() && !errorMessage() && patients().content.length > 0) { <div class="patient-list" role="list">@for (patient of patients().content; track patient.id) { <mat-card appearance="outlined" role="listitem" class="patient-card"><div class="avatar" aria-hidden="true">{{ initials(patient) }}</div><div class="identity"><h2>{{ patient.lastName }} {{ patient.firstName }}</h2><p>{{ patient.phone || patient.email || 'Coordonnées non renseignées' }}</p></div><span class="record-status">{{ patient.archivedAt ? 'Archivé' : 'Actif' }}</span><a mat-icon-button [routerLink]="['/dossier-patient', patient.id]" [attr.aria-label]="'Ouvrir le dossier de ' + patient.firstName + ' ' + patient.lastName"><mat-icon aria-hidden="true">arrow_forward</mat-icon></a></mat-card> }</div><nav class="pagination" aria-label="Pagination patients"><button mat-icon-button type="button" aria-label="Page précédente" [disabled]="patients().number === 0" (click)="load(patients().number - 1)"><mat-icon aria-hidden="true">chevron_left</mat-icon></button><span>Page {{ patients().number + 1 }} sur {{ patients().totalPages }}</span><button mat-icon-button type="button" aria-label="Page suivante" [disabled]="patients().number + 1 >= patients().totalPages" (click)="load(patients().number + 1)"></button></nav> }
    </section>
  `,
  styles: `
    :host { display: block; } .patients-page { max-width: 1100px; margin: auto; } .page-header { display: flex; justify-content: space-between; align-items: end; gap: 24px; margin-bottom: 32px; } .eyebrow { color: #0b7375; font-size: .72rem; font-weight: 800; letter-spacing: .16em; margin: 0 0 14px; } h1 { color: #173b3d; font-size: clamp(2.4rem, 5vw, 4.2rem); line-height: .95; margin: 0; } .subheading { color: #587174; margin: 14px 0 0; } .primary-action { background: #0b7375; color: #fff; min-height: 46px; } .search-row { display: flex; align-items: center; gap: 12px; margin-bottom: 22px; } .search-row mat-form-field { flex: 1; } .patient-list { display: grid; gap: 10px; } .patient-card { display: grid; grid-template-columns: auto 1fr auto auto; align-items: center; gap: 16px; padding: 16px; border-color: #d9e5e3; } .avatar { display: grid; place-items: center; width: 44px; height: 44px; border-radius: 50%; background: #e8f3f2; color: #0b7375; font-weight: 800; } .identity h2 { margin: 0; color: #173b3d; font-size: 1rem; } .identity p { margin: 4px 0 0; color: #71898a; } .record-status { color: #0b7375; font-size: .8rem; font-weight: 700; } .pagination { display: flex; align-items: center; justify-content: center; gap: 18px; margin: 24px 0; color: #587174; } .state, .empty { display: grid; place-items: center; gap: 10px; padding: 48px 24px; text-align: center; color: #587174; } .state mat-icon, .empty mat-icon { color: #e67e22; } .state.error { display: flex; justify-content: center; color: #a33c2f; } .empty h2 { margin: 0; color: #173b3d; } .empty p { margin: 0; } @media (max-width: 640px) { .page-header { align-items: start; flex-direction: column; } .search-row { align-items: stretch; flex-direction: column; } .search-row button { min-height: 46px; } .patient-card { grid-template-columns: auto 1fr auto; } .record-status { grid-column: 2; grid-row: 2; } }
  `,
})
export class PatientsListComponent {
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly api = inject(PatientsApi);
  protected readonly searchForm = this.formBuilder.group({ query: '' });
  protected readonly patients = signal<PageResponse<PatientResponse>>({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 });
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');

  constructor() { this.load(0); }

  search(): void { this.load(0); }
  load(page: number): void {
    this.loading.set(true); this.errorMessage.set('');
    this.api.search(this.searchForm.controls.query.value, page, 20).subscribe({ next: (response) => { this.patients.set(response); this.loading.set(false); }, error: () => { this.loading.set(false); this.errorMessage.set('Les patients sont momentanément indisponibles. Vérifiez votre connexion.'); } });
  }
  initials(patient: PatientResponse): string { return `${patient.firstName[0] ?? ''}${patient.lastName[0] ?? ''}`.toUpperCase(); }
}