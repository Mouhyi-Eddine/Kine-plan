export type MembershipRole = 'ADMIN' | 'KINESITHERAPEUTE' | 'SECRETAIRE' | 'SUPER_ADMIN';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface CabinetAccess {
  id: string;
  name: string;
  role: MembershipRole;
}

export interface LoginResponse {
  preAuthToken: string;
  cabinets: CabinetAccess[];
}

export interface SelectCabinetRequest {
  cabinetId: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresInSeconds: number;
}

export interface UserProfile {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  cabinetId: string;
  cabinetName: string;
  membershipId: string;
  role: MembershipRole;
}