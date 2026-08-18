// src/app/features/dashboard/dashboard.component.ts

import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <div style="padding: 4rem; text-align: center; font-family: sans-serif;">
      <h1>Welcome to the Dashboard! 🚀</h1>
      <p>If you can see this, your AuthGuard successfully let you in.</p>
      <button (click)="onLogout()" style="margin-top: 1rem; padding: 0.75rem 1.5rem; background: #ef4444; color: white; border: none; border-radius: 6px; cursor: pointer;">
        Log Out
      </button>
    </div>
  `
})
export class DashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  onLogout(): void {
      this.authService.logout().subscribe({
        next: () => {
          // Once the backend successfully revokes the token, redirect the user
          this.router.navigate(['/login']);
        },
        error: (err) => {
          console.error('Logout failed on the server, forcing local logout', err);
          // Even if the server crashes, we still want to kick the user out locally
          this.router.navigate(['/login']);
        }
      });
    }
}
