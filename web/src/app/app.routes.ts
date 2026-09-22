import { Routes } from '@angular/router';
import { AppShellComponent } from './layout/app-shell.component';
import { authenticatedGuard, cabinetGuard, roleGuard } from './core/auth/auth.guards';

export const routes: Routes = [
	{
		path: 'auth',
		loadChildren: () => import('./features/auth/auth.routes').then((module) => module.AUTH_ROUTES),
	},
	{
		path: 'onboarding',
		loadChildren: () => import('./features/onboarding/onboarding.routes').then((module) => module.ONBOARDING_ROUTES),
	},
	{
		path: '',
		component: AppShellComponent,
		canActivate: [authenticatedGuard, cabinetGuard],
		children: [
			{ path: 'cabinet', loadComponent: () => import('./features/cabinet/cabinet-profile.component').then((module) => module.CabinetProfileComponent) },
			{ path: 'dashboard', loadChildren: () => import('./features/dashboard/dashboard.routes').then((module) => module.DASHBOARD_ROUTES) },
			{ path: 'patients', loadChildren: () => import('./features/patients/patients.routes').then((module) => module.PATIENTS_ROUTES) },
			{ path: 'dossier-patient', loadChildren: () => import('./features/dossier-patient/dossier-patient.routes').then((module) => module.DOSSIER_PATIENT_ROUTES) },
			{ path: 'planning', loadChildren: () => import('./features/planning/planning.routes').then((module) => module.PLANNING_ROUTES) },
			{ path: 'liste-attente', loadChildren: () => import('./features/liste-attente/liste-attente.routes').then((module) => module.LISTE_ATTENTE_ROUTES) },
			{ path: 'praticiens', loadChildren: () => import('./features/praticiens/praticiens.routes').then((module) => module.PRATICIENS_ROUTES) },
			{ path: 'parametres-cabinet', loadChildren: () => import('./features/parametres-cabinet/parametres-cabinet.routes').then((module) => module.PARAMETRES_CABINET_ROUTES) },
			{ path: 'administration', canActivate: [roleGuard(['ADMIN'])], loadChildren: () => import('./features/administration/administration.routes').then((module) => module.ADMINISTRATION_ROUTES) },
			{ path: 'plateforme', canActivate: [roleGuard(['SUPER_ADMIN'])], loadChildren: () => import('./features/plateforme/plateforme.routes').then((module) => module.PLATEFORME_ROUTES) },
			{ path: '', pathMatch: 'full', redirectTo: 'dashboard' },
		],
	},
	{ path: '**', redirectTo: 'dashboard' },
];
