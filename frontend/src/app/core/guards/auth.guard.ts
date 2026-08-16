// src/app/core/guards/auth.guard.ts

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  // Inject our dependencies
  const authService = inject(AuthService);
  const router = inject(Router);

  // 1. Check if the user is logged in
  if (authService.isAuthenticated()) {
    return true; // Let them through to the protected route
  }

  // 2. If they are not logged in, boot them back to the login page safely
  return router.createUrlTree(['/login']);
};
