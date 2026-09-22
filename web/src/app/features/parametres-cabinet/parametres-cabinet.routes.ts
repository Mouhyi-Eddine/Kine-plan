import { Routes } from '@angular/router';
import { roleGuard } from '../../core/auth/auth.guards';
import { CabinetSettingsComponent } from './cabinet-settings.component';

export const PARAMETRES_CABINET_ROUTES: Routes = [{ path: '', component: CabinetSettingsComponent, canActivate: [roleGuard(['ADMIN'])], data: { title: 'Paramètres du cabinet' } }];
