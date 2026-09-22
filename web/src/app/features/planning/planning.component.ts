import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { DatePipe } from '@angular/common';
import { API_BASE_URL } from '../../core/api/api.config';

interface Appointment { id: string; patientId: string; practitionerMembershipId: string; startAt: string; endAt: string; status: string; source: string; cancellationReason?: string; }
interface Page<T> { content: T[]; totalPages: number; number: number; }

@Component({ selector: 'app-planning', changeDetection: ChangeDetectionStrategy.OnPush, imports: [DatePipe, MatButtonModule, MatCardModule, MatIconModule], template: `
<section class="page" aria-labelledby="planning-title"><header><div><p class="eyebrow">AGENDA / CABINET ACTIF</p><h1 id="planning-title">Planning</h1><p class="intro">Les rendez-vous du jour dans ce cabinet uniquement.</p></div><button mat-flat-button type="button"><mat-icon aria-hidden="true">add</mat-icon>Nouveau rendez-vous</button></header>
@if (loading()) { <p class="state" role="status">Chargement du planning...</p> } @if (error()) { <p class="state error" role="alert">Le planning est momentanément indisponible.</p> } @if (!loading() && !error() && appointments().length === 0) { <mat-card appearance="outlined" class="empty">Aucun rendez-vous prévu aujourd'hui.</mat-card> } @for (appointment of appointments(); track appointment.id) { <mat-card appearance="outlined" class="appointment"><div class="time">{{ appointment.startAt | date:'HH:mm' }}<small>{{ appointment.endAt | date:'HH:mm' }}</small></div><div><strong>Patient {{ appointment.patientId }}</strong><p>Praticien {{ appointment.practitionerMembershipId }}</p></div><span class="status">{{ appointment.status }}</span><span class="source">{{ appointment.source }}</span></mat-card> }</section>`, styles: `.page{max-width:1100px;margin:auto}header{display:flex;justify-content:space-between;align-items:end;gap:20px;margin-bottom:32px}button{background:#0b7375;color:#fff;min-height:44px}.eyebrow{color:#0b7375;font-size:.72rem;font-weight:800;letter-spacing:.16em;margin:0 0 14px}h1{color:#173b3d;font-size:clamp(2.4rem,5vw,4.2rem);line-height:.95;margin:0}.intro{color:#587174}.appointment{display:grid;grid-template-columns:100px 1fr auto auto;align-items:center;gap:20px;padding:18px;margin-bottom:10px;border-color:#d9e5e3}.time{font-size:1.3rem;font-weight:800;color:#173b3d}.time small{display:block;font-size:.8rem;color:#71898a}.appointment p{margin:5px 0 0;color:#71898a}.status,.source{font-size:.78rem;font-weight:700;color:#0b7375}.state,.empty{padding:40px;color:#587174}.error{color:#a33c2f}@media(max-width:700px){header{align-items:start;flex-direction:column}.appointment{grid-template-columns:70px 1fr}.status,.source{grid-column:2}}`, })
export class PlanningComponent {
  private readonly http = inject(HttpClient); private readonly baseUrl = inject(API_BASE_URL);
  protected readonly appointments = signal<Appointment[]>([]); protected readonly loading = signal(true); protected readonly error = signal(false);
  constructor() { const from = new Date(); from.setHours(0,0,0,0); const to = new Date(from); to.setDate(to.getDate()+1); const params = new HttpParams().set('from', from.toISOString()).set('to', to.toISOString()).set('page', 0).set('size', 100).set('sort', 'startAt,asc'); this.http.get<Page<Appointment>>(`${this.baseUrl}/appointments`, { params }).subscribe({ next: page => { this.appointments.set(page.content); this.loading.set(false); }, error: () => { this.error.set(true); this.loading.set(false); } }); }
}
