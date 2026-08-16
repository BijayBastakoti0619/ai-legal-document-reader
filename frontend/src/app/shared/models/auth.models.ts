export interface LoginRequest{
  email: string;
  password: string;
}

export interface LoginResponse{
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface RefreshRequest{
  refreshToken: string;
}

export interface UserProfile{
  id: number;
  email: string;
  displayName: string;
  role: string;
}
