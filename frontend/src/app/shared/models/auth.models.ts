// src/app/shared/models/auth.models.ts

// --- REGISTRATION ---
export interface RegisterRequest {
  email: string;
  password: string;
  displayName: string;
}

export interface RegisterResponse {
  id: number;
  email: string;
  displayName: string;
  role: 'USER' | 'ADMIN'; // Stricter typing from your friend's commit
}

// --- LOGIN ---
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

// --- SESSION & USER ---
export interface RefreshRequest {
  refreshToken: string;
}

export interface UserProfile {
  id: number;
  email: string;
  displayName: string;
  role: string;
}
