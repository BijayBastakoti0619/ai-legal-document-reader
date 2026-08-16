// src/app/core/services/auth.service.ts

import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, UserProfile, RefreshRequest } from '../../shared/models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private authState = signal<boolean>(this.hasToken());

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

  getUserProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${environment.apiUrl}/users/me`);
  }

  // --- NEW: Token Refresh Method ---
  refreshToken(): Observable<LoginResponse> {
    // 1. Grab the current refresh token from storage
    const refreshToken = localStorage.getItem('refreshToken') || '';
    const request: RefreshRequest = { refreshToken };

    // 2. Call the backend to get a fresh pair of tokens
    return this.http.post<LoginResponse>(
      `${environment.apiUrl}/auth/refresh`,
      request
    ).pipe(
      tap((response: LoginResponse) => {
        // 3. Save the new tokens and ensure the user stays logged in
        localStorage.setItem('accessToken', response.accessToken);
        localStorage.setItem('refreshToken', response.refreshToken);
        this.authState.set(true);
      })
    );
  }

  isAuthenticated(): boolean {
    return this.authState();
  }

  private hasToken(): boolean {
    return !!localStorage.getItem('accessToken');
  }
}
