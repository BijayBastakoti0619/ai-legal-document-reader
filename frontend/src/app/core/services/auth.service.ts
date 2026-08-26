// src/app/core/services/auth.service.ts

import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RegisterRequest, RegisterResponse, LoginRequest, LoginResponse, UserProfile, RefreshRequest } from '../../shared/models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private authState = signal<boolean>(this.hasToken());

  // --- REGISTRATION FLOW (Restored from your friend's commit) ---
  register(request: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(
      `${environment.apiUrl}/auth/register`,
      request
    );
  }

  // --- LOGIN FLOW ---
  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${environment.apiUrl}/auth/login`,
      request

    ).pipe(
      tap((response: LoginResponse) => {
        localStorage.setItem('accessToken', response.accessToken);
        localStorage.setItem('refreshToken', response.refreshToken);
        this.authState.set(true);
      })
    );
  }

  // --- LOGOUT FLOW ---
  logout(): Observable<void> {
    const refreshToken = localStorage.getItem('refreshToken') || '';
    const request: RefreshRequest = { refreshToken };

    return this.http.post<void>(
      `${environment.apiUrl}/auth/logout`,
      request
    ).pipe(
      tap(() => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        this.authState.set(false);
      })
    );
  }

  // --- USER PROFILE ---
  getUserProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${environment.apiUrl}/users/me`);
  }

  // --- TOKEN REFRESH METHOD ---
  refreshToken(): Observable<LoginResponse> {
    const refreshToken = localStorage.getItem('refreshToken') || '';
    const request: RefreshRequest = { refreshToken };

    return this.http.post<LoginResponse>(
      `${environment.apiUrl}/auth/refresh`,
      request
    ).pipe(
      tap((response: LoginResponse) => {
        localStorage.setItem('accessToken', response.accessToken);
        localStorage.setItem('refreshToken', response.refreshToken);
        this.authState.set(true);
      })
    );
  }

  // --- STATE MANAGEMENT ---
  isAuthenticated(): boolean {
    return this.authState();
  }

  private hasToken(): boolean {
    return !!localStorage.getItem('accessToken');
  }
}
