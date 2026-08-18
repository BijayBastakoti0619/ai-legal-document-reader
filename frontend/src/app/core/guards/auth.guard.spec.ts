// src/app/core/guards/auth.guard.spec.ts

import { TestBed } from '@angular/core/testing';
import { Router, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  let router: Router;
  let mockAuthService: Partial<AuthService>;

  beforeEach(() => {
    // Create a simple mock object for Vitest compatibility
    mockAuthService = {
      isAuthenticated: () => false // default state
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService }
      ]
    });

    router = TestBed.inject(Router);
  });

  it('should allow route activation if user is authenticated', () => {
    // Arrange: Tell the mock service the user IS logged in
    mockAuthService.isAuthenticated = () => true;

    // Act: Run the guard in the Angular injection context
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    // Assert: The guard should return true (allow access)
    expect(result).toBe(true);
  });

  it('should redirect to /login if user is NOT authenticated', () => {
    // Arrange: Tell the mock service the user is NOT logged in
    mockAuthService.isAuthenticated = () => false;

    // Act
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    // Assert: The guard should return an Angular UrlTree redirecting to login
    expect(result).toEqual(router.createUrlTree(['/login']));
  });
});
