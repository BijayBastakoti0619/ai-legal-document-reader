import { Injectable, inject } from '@angular/core';
import {
  HttpClient,
  HttpEvent
} from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  DocumentUploadResponse,
  DocumentSummary,
  DocumentDetail,
  PaginatedResponse
} from '../../shared/models/document.models';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {

  private readonly http = inject(HttpClient);

  // --- EXISTING: Upload Document (Cleaned of localStorage hacks) ---
  uploadDocument(
    file: File
  ): Observable<HttpEvent<DocumentUploadResponse>> {

    const formData = new FormData();
    formData.append('file', file, file.name);

    return this.http.post<DocumentUploadResponse>(
      `${environment.apiUrl}/documents`,
      formData,
      {
        reportProgress: true,
        observe: 'events'
      }
    );
  }

  // --- NEW: Fetch Paginated History ---
  getDocuments(
    page: number = 0,
    size: number = 10
  ): Observable<PaginatedResponse<DocumentSummary>> {

    return this.http.get<PaginatedResponse<DocumentSummary>>(
      `${environment.apiUrl}/documents?page=${page}&size=${size}`
    );
  }

  // --- NEW: Fetch Specific Document Detail Metadata ---
  getDocument(
    id: number
  ): Observable<DocumentDetail> {

    return this.http.get<DocumentDetail>(
      `${environment.apiUrl}/documents/${id}`
    );
  }

  // --- EXISTING: Download PDF Content ---
  getDocumentContent(
    documentId: number
  ): Observable<Blob> {

    return this.http.get(
      `${environment.apiUrl}/documents/${documentId}/content`,
      {
        responseType: 'blob'
      }
    );
  }
}
