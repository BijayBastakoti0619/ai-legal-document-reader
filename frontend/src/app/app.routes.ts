// src/app/app.routes.ts

import { Routes } from '@angular/router';
import { RegisterComponent } from './features/auth/register/register.component';
import { LoginComponent } from './features/auth/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
    {
      path: 'register',
      component: RegisterComponent
    },
  {
    path: 'login',
    component: LoginComponent
  },
 {
    path: '',
    redirectTo: 'register',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard] // <-- This is where our bouncer stands!
  },
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
