import { Routes } from '@angular/router';
import { PatientRecordComponent } from './patient-record.component';

export const DOSSIER_PATIENT_ROUTES: Routes = [{ path: ':patientId', component: PatientRecordComponent, data: { title: 'Dossier patient' } }];
