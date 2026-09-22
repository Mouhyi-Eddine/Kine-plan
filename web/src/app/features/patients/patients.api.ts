import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../core/api/api.config';
import { PageResponse, PatientResponse } from './patients.models';

@Injectable({ providedIn: 'root' })
export class PatientsApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  search(query: string, page: number, size: number): Observable<PageResponse<PatientResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'lastName,asc');
    if (query.trim()) params = params.set('query', query.trim());
    return this.http.get<PageResponse<PatientResponse>>(`${this.baseUrl}/patients`, { params });
  }
}