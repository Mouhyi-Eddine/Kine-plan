import { Routes } from '@angular/router';
import { PlanningComponent } from './planning.component';
import { AppointmentFormComponent } from './appointment-form.component';

export const PLANNING_ROUTES: Routes = [{ path: 'new', component: AppointmentFormComponent, data: { title: 'Nouveau rendez-vous' } }, { path: '', component: PlanningComponent, data: { title: 'Planning' } }];
