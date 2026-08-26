// src/app/features/documents/document-history/document-history.component.spec.ts

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router'; // <-- NEW: Import the router provider
import { of, throwError, Subject } from 'rxjs';
import { vi } from 'vitest';
import { DocumentHistoryComponent } from './document-history.component';
import { DocumentService } from '../../../core/services/document.service';
import { PaginatedResponse, DocumentSummary } from '../../../shared/models/document.models';

describe('DocumentHistoryComponent', () => {
  let component: DocumentHistoryComponent;
  let fixture: ComponentFixture<DocumentHistoryComponent>;

  // Mock the service so we control exactly what the API returns
  const mockDocumentService = {
    getDocuments: vi.fn()
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [DocumentHistoryComponent],
      providers: [
        { provide: DocumentService, useValue: mockDocumentService },
        provideRouter([]) // <-- FIX: Satisfies RouterLink's dependency
      ]
    }).compileComponents();
  });

  function createComponent() {
    fixture = TestBed.createComponent(DocumentHistoryComponent);
    component = fixture.componentInstance;
  }

  it('should show the loading state initially', () => {
    // Arrange: Return an unresolved Subject to simulate a pending HTTP request
    mockDocumentService.getDocuments.mockReturnValue(new Subject());

    // Act
    createComponent();
    fixture.detectChanges(); // Triggers ngOnInit

    // Assert
    expect(component.isLoading()).toBe(true);
  });

  it('should show the empty state when no documents exist', () => {
    // Arrange: Simulate an empty page from the backend
    const emptyPage: PaginatedResponse<DocumentSummary> = {
      content: [],
      page: { size: 10, number: 0, totalElements: 0, totalPages: 0 }
    };
    mockDocumentService.getDocuments.mockReturnValue(of(emptyPage));

    // Act
    createComponent();
    fixture.detectChanges();

    // Assert
    expect(component.isLoading()).toBe(false);
    expect(component.documents().length).toBe(0);
    expect(component.hasError()).toBe(false);
  });

  it('should show the error state if the API fails', () => {
    // Arrange: Simulate a 500 Internal Server Error
    mockDocumentService.getDocuments.mockReturnValue(throwError(() => new Error('API Crash')));

    // Act
    createComponent();
    fixture.detectChanges();

    // Assert
    expect(component.isLoading()).toBe(false);
    expect(component.hasError()).toBe(true);
  });

  it('should display documents on a successful fetch', () => {
    // Arrange: Simulate a successful backend response with 1 document
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
    mockDocumentService.getDocuments.mockReturnValue(of(mockPage));

    // Act
    createComponent();
    fixture.detectChanges();

    // Assert
    expect(component.isLoading()).toBe(false);
    expect(component.documents().length).toBe(1);
    expect(component.documents()[0].originalFilename).toBe('lease.pdf');
  });
});
