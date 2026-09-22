import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { DecimalPipe } from '@angular/common';
import { API_BASE_URL } from '../../core/api/api.config';

interface DashboardResponse { appointments: number; absences: number; absenceRate: number; }

@Component({
  selector: 'app-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DecimalPipe, MatCardModule, MatIconModule],
  template: `
    <section class="page" aria-labelledby="dashboard-title">
      <p class="eyebrow">CABINET ACTIF</p><h1 id="dashboard-title">Tableau de bord</h1>
      <p class="intro">La journée de votre cabinet en un coup d'oeil.</p>
      @if (loading()) { <p class="state" role="status">Chargement des indicateurs...</p> }
      @if (error()) { <p class="state error" role="alert">Les indicateurs sont momentanément indisponibles.</p> }
      @if (data(); as metrics) { <div class="metrics"><mat-card appearance="outlined"><mat-icon aria-hidden="true">event</mat-icon><strong>{{ metrics.appointments }}</strong><span>Rendez-vous sur la période</span></mat-card><mat-card appearance="outlined"><mat-icon aria-hidden="true">person_off</mat-icon><strong>{{ metrics.absences }}</strong><span>Absences</span></mat-card><mat-card appearance="outlined"><mat-icon aria-hidden="true">percent</mat-icon><strong>{{ metrics.absenceRate | number:'1.0-1' }} %</strong><span>Taux d'absence</span></mat-card></div> }
    </section>
  `,
  styles: `.page{max-width:1100px;margin:auto}.eyebrow{color:#0b7375;font-size:.72rem;font-weight:800;letter-spacing:.16em;margin:0 0 14px}h1{color:#173b3d;font-size:clamp(2.4rem,5vw,4.2rem);line-height:.95;margin:0}.intro{color:#587174;margin:14px 0 32px}.metrics{display:grid;grid-template-columns:repeat(3,1fr);gap:16px}.metrics mat-card{padding:24px;display:grid;gap:10px;border-color:#d9e5e3}.metrics mat-icon{color:#e67e22}.metrics strong{font-size:2.2rem;color:#173b3d}.metrics span{color:#587174}.state{padding:32px;color:#587174}.error{color:#a33c2f}@media(max-width:700px){.metrics{grid-template-columns:1fr}}`,
})
export class DashboardComponent {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);
  protected readonly data = signal<DashboardResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal(false);
  constructor() {
    const to = new Date(); const from = new Date(to); from.setHours(0, 0, 0, 0); to.setHours(23, 59, 59, 999);
    const params = new HttpParams().set('from', from.toISOString()).set('to', to.toISOString());
    this.http.get<DashboardResponse>(`${this.baseUrl}/dashboard/appointments`, { params }).subscribe({ next: value => { this.data.set(value); this.loading.set(false); }, error: () => { this.error.set(true); this.loading.set(false); } });
  }
}
