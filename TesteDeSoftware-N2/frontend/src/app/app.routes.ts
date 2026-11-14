import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { ShellComponent } from './layout/shell.component';
import { LoginComponent } from './features/login/login.component';
import { RegisterComponent } from './features/register/register.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { PdvComponent } from './features/pdv/pdv.component';
import { CatalogComponent } from './features/catalog/catalog.component';
import { SalesComponent } from './features/sales/sales.component';

export const routes: Routes = [
	{ path: '', pathMatch: 'full', redirectTo: 'app/dashboard' },
	{ path: 'login', component: LoginComponent },
	{ path: 'cadastro', component: RegisterComponent },
	{
		path: 'app',
		component: ShellComponent,
		canActivate: [authGuard],
		children: [
			{ path: 'dashboard', component: DashboardComponent },
			{ path: 'pdv', component: PdvComponent },
			{ path: 'catalogo', component: CatalogComponent },
			{ path: 'vendas', component: SalesComponent },
			{ path: '', pathMatch: 'full', redirectTo: 'dashboard' },
		],
	},
	{ path: '**', redirectTo: 'app/dashboard' },
];
