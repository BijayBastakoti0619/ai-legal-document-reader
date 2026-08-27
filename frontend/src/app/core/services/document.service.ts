import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpEvent, HttpEventType } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  DocumentType,
  DocumentUploadResponse,
  DocumentDetail // <-- NEW: Import the detail model
} from '../../shared/models/document.models';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private readonly http = inject(HttpClient);
  private readonly storageKey = 'recentUploadedDocuments';

  private readonly recentUploadsSignal = signal<DocumentUploadResponse[]>(this.loadRecentUploads());
  readonly recentUploads = this.recentUploadsSignal.asReadonly();

  uploadDocument(file: File, documentType: DocumentType): Observable<HttpEvent<DocumentUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    formData.append('documentType', documentType);

    return this.http.post<DocumentUploadResponse>(
      `${environment.apiUrl}/documents`,
      formData,
      {
        reportProgress: true,
        observe: 'events'
      }
    ).pipe(
      tap(event => {
        if (event.type === HttpEventType.Response && event.body) {
          this.addRecentUpload(event.body);
        }
      })
    );
  }

  private addRecentUpload(document: DocumentUploadResponse): void {
    const currentDocuments = this.recentUploadsSignal();
    const updatedDocuments: DocumentUploadResponse[] = [
      document,
      ...currentDocuments.filter(item => item.id !== document.id)
    ].slice(0, 6);

    this.recentUploadsSignal.set(updatedDocuments);
    localStorage.setItem(this.storageKey, JSON.stringify(updatedDocuments));
  }
  removeRecentUpload(
    documentId: number
  ): void {

    const updatedDocuments =
      this.recentUploadsSignal()
        .filter(
          document =>
            document.id !== documentId
        );

    this.recentUploadsSignal.set(
      updatedDocuments
    );

    localStorage.setItem(
      this.storageKey,
      JSON.stringify(updatedDocuments)
    );
  }


  private loadRecentUploads(): DocumentUploadResponse[] {
    const stored = localStorage.getItem(this.storageKey);
    if (!stored) {
      return [];
    }

    try {
      const documents = JSON.parse(stored) as DocumentUploadResponse[];
      return documents.map(document => ({
        ...document,
        documentType: document.documentType ?? 'OTHER'
      }));
    } catch {
      return [];
    }
  }

  getDocumentContent(documentId: number): Observable<Blob> {
    return this.http.get(
      `${environment.apiUrl}/documents/${documentId}/content`,
      { responseType: 'blob' }
    );
  }

  // --- NEW: Restored Methods for Details and History Grid ---

// --- RESTORED: Now returning the correct DocumentDetail model ---
  getDocument(id: number): Observable<DocumentDetail> {
    return this.http.get<DocumentDetail>(
      `${environment.apiUrl}/documents/${id}`
    );
  }

  getDocuments(page: number, size: number): Observable<any> {
    return this.http.get<any>(
      `${environment.apiUrl}/documents`, {
        params: {
          page: page.toString(),
          size: size.toString()
        }
      }
    );
  }

  deleteDocument(
    documentId: number
  ): Observable<void> {

    return this.http.delete<void>(
      `${environment.apiUrl}/documents/${documentId}`
    );
  }
}
