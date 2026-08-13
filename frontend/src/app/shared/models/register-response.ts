export interface RegisterResponse {
  id: number;
  email: string;
  displayName: string;
  role: 'USER' | 'ADMIN';
}
