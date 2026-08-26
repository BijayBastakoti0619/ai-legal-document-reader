// src/app/core/services/document.service.spec.ts

import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { DocumentService } from './document.service';
import { environment } from '../../../environments/environment';
import {
  DocumentUploadResponse,
  DocumentSummary,
  DocumentDetail,
  PaginatedResponse
} from '../../shared/models/document.models';

describe('DocumentService', () => {
  let service: DocumentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DocumentService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(DocumentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should successfully upload a document', () => {
    // Arrange
    const mockFile = new File(['dummy content'], 'contract.pdf', { type: 'application/pdf' });
    const mockResponse: DocumentUploadResponse = {
      id: 1,
      originalFilename: 'contract.pdf',
      contentType: 'application/pdf',
      fileSize: 13,
      status: 'UPLOADED',
      createdAt: '2026-08-26T12:00:00Z'
    };

    // Act
    service.uploadDocument(mockFile).subscribe();

    // Assert (HTTP Level)
    const req = httpMock.expectOne(`${environment.apiUrl}/documents`);
    expect(req.request.method).toBe('POST');

    // FIX: Using Vitest's .toBe(true) instead of Jasmine's .toBeTrue()
    expect(req.request.body instanceof FormData).toBe(true);
    expect(req.request.reportProgress).toBe(true);

    // Simulate the backend returning our mock response
    req.flush(mockResponse);
  });

  it('should fetch a paginated list of document summaries', () => {
    // Arrange
    const mockPage: PaginatedResponse<DocumentSummary> = {
      content: [{
        id: 15,
        originalFilename: 'lease.pdf',
        fileSize: 1024,
        status: 'UPLOADED',
        createdAt: '2026-08-18T20:30:00Z'
      }],
      page: { size: 10, number: 0, totalElements: 1, totalPages: 1 }
    };

    // Act
    service.getDocuments(0, 10).subscribe(response => {
      // Assert
      expect(response.content.length).toBe(1);
      expect(response.content[0].originalFilename).toBe('lease.pdf');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/documents?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should fetch document details by id', () => {
    // Arrange
    const mockDetail: DocumentDetail = {
      id: 15,
      originalFilename: 'lease.pdf',
      contentType: 'application/pdf',
      fileSize: 1024,
      status: 'UPLOADED',
      createdAt: '2026-08-18T20:30:00Z',
      updatedAt: '2026-08-18T20:30:00Z'
    };

    // Act
    service.getDocument(15).subscribe(detail => {
      // Assert
      expect(detail.id).toBe(15);
      expect(detail.status).toBe('UPLOADED');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/documents/15`);
    expect(req.request.method).toBe('GET');
    req.flush(mockDetail);
  });

  it('should fetch document content as a blob', () => {
    // Arrange
    const mockBlob = new Blob(['pdf-data'], { type: 'application/pdf' });

    // Act
    service.getDocumentContent(15).subscribe(blob => {
      // Assert
      expect(blob).toBeTruthy();
      expect(blob.size).toBeGreaterThan(0);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/documents/15/content`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(mockBlob);
  });
});
