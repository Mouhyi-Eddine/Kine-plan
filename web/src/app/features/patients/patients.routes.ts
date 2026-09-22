import { Routes } from '@angular/router';
import { FeaturePlaceholderComponent } from '../../shared/feature-placeholder.component';
import { PatientsListComponent } from './patients-list.component';

export const PATIENTS_ROUTES: Routes = [{ path: '', component: PatientsListComponent }];
