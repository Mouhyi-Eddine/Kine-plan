import { Routes } from '@angular/router';
import { FeaturePlaceholderComponent } from '../../shared/feature-placeholder.component';
import { LoginComponent } from './login.component';
import { SelectCabinetComponent } from './select-cabinet.component';

export const AUTH_ROUTES: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'select-cabinet', component: SelectCabinetComponent },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
];
