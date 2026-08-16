// src/app/core/interceptors/auth.interceptor.spec.ts

import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';
import { of, throwError } from 'rxjs';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let mockAuthService: Partial<AuthService>;

  beforeEach(() => {
    // We mock the AuthService so we can control how the refresh behaves in tests
    mockAuthService = {
      refreshToken: () => of({ accessToken: 'new-mock-token', refreshToken: 'new-refresh', tokenType: 'Bearer', expiresIn: 900 }),
      logout: () => of(undefined)
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: mockAuthService }
      ]
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);

    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should attach the Authorization header when an access token exists', () => {
    localStorage.setItem('accessToken', 'mock-jwt-token');
    http.get('/api/protected-data').subscribe();

    const req = httpMock.expectOne('/api/protected-data');
    expect(req.request.headers.has('Authorization')).toBe(true);
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-jwt-token');
    req.flush({});
  });

  it('should NOT attach the Authorization header if no token exists', () => {
    http.get('/api/public-data').subscribe();

    const req = httpMock.expectOne('/api/public-data');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  // --- OUR NEW REFRESH TOKEN TESTS ---

  it('should catch 401, trigger refresh, and retry the request', () => {
    localStorage.setItem('accessToken', 'expired-token');

    // Wiretap our mock to track if it was called
    let refreshCalled = false;
    mockAuthService.refreshToken = () => {
      refreshCalled = true;
      localStorage.setItem('accessToken', 'new-mock-token');
      return of({ accessToken: 'new-mock-token', refreshToken: 'new-refresh', tokenType: 'Bearer', expiresIn: 900 });
    };

    http.get('/api/protected-data').subscribe();

    // 1. Fail the first request with a 401 Unauthorized
    const req1 = httpMock.expectOne('/api/protected-data');
    req1.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    // 2. Verify the interceptor triggered the refresh
    expect(refreshCalled).toBe(true);

    // 3. Verify the interceptor cloned and retried the request with the NEW token
    const req2 = httpMock.expectOne('/api/protected-data');
    expect(req2.request.headers.get('Authorization')).toBe('Bearer new-mock-token');
    req2.flush({});
  });

  it('should logout if the refresh token request fails', () => {
    localStorage.setItem('accessToken', 'expired-token');

    // Force the refresh to fail with an error
    let logoutCalled = false;
    mockAuthService.refreshToken = () => throwError(() => new HttpErrorResponse({ status: 403 }));
    mockAuthService.logout = () => {
      logoutCalled = true;
      return of(undefined);
    };

    http.get('/api/protected-data').subscribe({
      error: () => {} // Catch the error so it doesn't crash the test runner
    });

    const req1 = httpMock.expectOne('/api/protected-data');
    req1.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    // The interceptor should have triggered a logout when the refresh failed
    expect(logoutCalled).toBe(true);
  });
});
