// src/app/app.routes.ts

// src/app/app.routes.ts

import { Routes } from '@angular/router';
import { RegisterComponent } from './features/auth/register/register.component';
import { LoginComponent } from './features/auth/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { DocumentUploadComponent } from './features/documents/upload/document-upload.component';
import { authGuard } from './core/guards/auth.guard';
import {DocumentAnalysisComponent} from './features/documents/analysis/document-analysis.component';

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
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'documents/upload',
    component: DocumentUploadComponent,
    canActivate: [authGuard]
  },
  {
    path: 'documents/:id/analysis',
    component: DocumentAnalysisComponent,
    canActivate: [authGuard]
  },
  {
    path: '',
    redirectTo: 'register',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
