import { Routes } from '@angular/router';
import { WaitingListComponent } from './waiting-list.component';

export const LISTE_ATTENTE_ROUTES: Routes = [{ path: '', component: WaitingListComponent, data: { title: 'Liste d’attente' } }];
