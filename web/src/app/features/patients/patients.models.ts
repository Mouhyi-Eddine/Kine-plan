export interface PatientResponse {
  id: string;
  firstName: string;
  lastName: string;
  birthDate?: string;
  phone?: string;
  email?: string;
  emergencyContact?: Record<string, string>;
  smsConsent: boolean;
  emailConsent: boolean;
  rgpdConsent: boolean;
  archivedAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}