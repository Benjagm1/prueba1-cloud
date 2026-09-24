export interface SessionUser {
  token: string;
  userId: number;
  email: string;
  tipo: string;
}

export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  tipo: string;
}
