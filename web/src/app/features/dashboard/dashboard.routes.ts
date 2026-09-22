import { Routes } from '@angular/router';
import { FeaturePlaceholderComponent } from '../../shared/feature-placeholder.component';

export const DASHBOARD_ROUTES: Routes = [{ path: '', component: FeaturePlaceholderComponent, data: { title: 'Tableau de bord' } }];
