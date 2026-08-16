// src/app/core/services/auth.service.spec.ts

import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, UserProfile, RefreshRequest } from '../../shared/models/auth.models';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);

    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should execute login, save tokens to localStorage, and update state', () => {
    const mockRequest: LoginRequest = { email: 'user@example.com', password: 'StrongPassword123!' };
    const mockResponse: LoginResponse = {
      accessToken: 'mock-access-token',
      refreshToken: 'mock-refresh-token',
      tokenType: 'Bearer',
      expiresIn: 900
    };

    service.login(mockRequest).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);

    expect(localStorage.getItem('accessToken')).toBe('mock-access-token');
    expect(localStorage.getItem('refreshToken')).toBe('mock-refresh-token');
    expect(service.isAuthenticated()).toBe(true);
  });

  it('should call the logout endpoint, clear localStorage, and update state', () => {
    localStorage.setItem('accessToken', 'fake-token');
    localStorage.setItem('refreshToken', 'fake-refresh');

    service.logout().subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/logout`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ refreshToken: 'fake-refresh' });
    req.flush(null);

    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should fetch the current user profile', () => {
    const mockProfile: UserProfile = {
      id: 1,
      email: 'user@example.com',
      displayName: 'Sample User',
      role: 'USER'
    };

    service.getUserProfile().subscribe(profile => {
      expect(profile).toEqual(mockProfile);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/users/me`);
    expect(req.request.method).toBe('GET');
    req.flush(mockProfile);
  });

  // --- OUR NEW REFRESH TOKEN TEST ---
  it('should refresh the token, save new tokens, and update state', () => {
    // Arrange: Give the test an existing refresh token
    localStorage.setItem('refreshToken', 'old-refresh-token');

    const mockResponse: LoginResponse = {
      accessToken: 'new-access-token',
      refreshToken: 'new-refresh-token', // Backend issues a rotated refresh token
      tokenType: 'Bearer',
      expiresIn: 900
    };

    // Act
    service.refreshToken().subscribe();

    // Assert (HTTP Level)
    const req = httpMock.expectOne(`${environment.apiUrl}/auth/refresh`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ refreshToken: 'old-refresh-token' });
    req.flush(mockResponse); // Simulate backend returning new tokens

    // Assert (State Level)
    expect(localStorage.getItem('accessToken')).toBe('new-access-token');
    expect(localStorage.getItem('refreshToken')).toBe('new-refresh-token');
    expect(service.isAuthenticated()).toBe(true);
  });
});
