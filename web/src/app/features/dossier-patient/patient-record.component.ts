import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { API_BASE_URL } from '../../core/api/api.config';

interface Prescription {
  id: string;
  prescriber: string;
  prescribedAt: string;
  sessionsPrescribed: number;
  sessionsConsumed: number;
  expiresAt?: string;
}

interface Note {
  id: string;
  content: string;
  appointmentId?: string;
}

@Component({
  selector: 'app-patient-record',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, MatCardModule, MatIconModule, RouterLink],
  template: `
    <section class="page" aria-labelledby="record-title">
      <a routerLink="/patients">← Retour aux patients</a>
      <p class="eyebrow">DOSSIER PATIENT</p>
      <h1 id="record-title">Dossier clinique</h1>

      @if (loading()) { <p class="state" role="status">Chargement du dossier...</p> }
      @if (error()) { <p class="state error" role="alert">Le dossier est momentanément indisponible.</p> }

      @if (!loading() && !error()) {
        <div class="columns">
          <section aria-labelledby="prescriptions-title">
            <h2 id="prescriptions-title">Ordonnances</h2>
            @for (prescription of prescriptions(); track prescription.id) {
              <mat-card appearance="outlined" class="item">
                <strong>{{ prescription.prescriber }}</strong>
                <span>{{ prescription.prescribedAt | date:'dd/MM/yyyy' }} · {{ prescription.sessionsConsumed }} / {{ prescription.sessionsPrescribed }} séances</span>
              </mat-card>
            }
            @if (!prescriptions().length) { <p class="muted">Aucune ordonnance enregistrée.</p> }
          </section>

          <section aria-labelledby="notes-title">
            <h2 id="notes-title">Notes de séance</h2>
            @if (notesForbidden()) {
              <p class="state forbidden" role="alert">Accès non autorisé à ce contenu.</p>
            } @else if (notesError()) {
              <p class="state error" role="alert">Les notes cliniques sont momentanément indisponibles.</p>
            } @else {
              @for (note of notes(); track note.id) {
                <mat-card appearance="outlined" class="item"><p>{{ note.content }}</p></mat-card>
              }
              @if (!notes().length) { <p class="muted">Aucune note clinique enregistrée.</p> }
            }
          </section>
        </div>
      }
    </section>
  `,
  styles: `
    .page { max-width: 1100px; margin: auto; }
    .page > a { color: #0b7375; text-decoration: none; }
    .eyebrow { color: #0b7375; font-size: .72rem; font-weight: 800; letter-spacing: .16em; margin: 32px 0 14px; }
    h1 { color: #173b3d; font-size: clamp(2.4rem, 5vw, 4.2rem); line-height: .95; margin: 0 0 32px; }
    .columns { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
    h2 { color: #173b3d; }
    .item { padding: 16px; margin-bottom: 10px; border-color: #d9e5e3; }
    .item span, .muted { display: block; color: #71898a; margin-top: 7px; }
    .state { padding: 24px 0; color: #587174; }
    .error { color: #a33c2f; }
    .forbidden { color: #8a5a13; }
    @media (max-width: 700px) { .columns { grid-template-columns: 1fr; } }
  `,
})
export class PatientRecordComponent {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);
  private readonly route = inject(ActivatedRoute);

  protected readonly prescriptions = signal<Prescription[]>([]);
  protected readonly notes = signal<Note[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal(false);
  protected readonly notesForbidden = signal(false);
  protected readonly notesError = signal(false);

  constructor() {
    const patientId = this.route.snapshot.paramMap.get('patientId');
    if (!patientId) {
      this.error.set(true);
      this.loading.set(false);
      return;
    }

    this.http.get<Prescription[]>(`${this.baseUrl}/patients/${patientId}/prescriptions`).subscribe({
      next: (value) => this.prescriptions.set(value),
      error: () => undefined,
    });
    this.http.get<Note[]>(`${this.baseUrl}/patients/${patientId}/clinical-notes`).subscribe({
      next: (value) => this.notes.set(value),
      error: (response: HttpErrorResponse) => {
        if (response.status === 403) {
          this.notesForbidden.set(true);
        } else {
          this.notesError.set(true);
        }
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }
}
