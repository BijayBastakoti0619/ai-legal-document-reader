// src/app/features/documents/document-detail/document-detail.component.spec.ts

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, ActivatedRoute, convertToParamMap } from '@angular/router';
import { of, throwError, Subject } from 'rxjs';
import { vi } from 'vitest';
import { DocumentDetailComponent } from './document-detail.component';
import { DocumentService } from '../../../core/services/document.service';
import { DocumentDetail } from '../../../shared/models/document.models';

describe('DocumentDetailComponent', () => {
  let component: DocumentDetailComponent;
  let fixture: ComponentFixture<DocumentDetailComponent>;

  // Mock the DocumentService
  const mockDocumentService = {
    getDocument: vi.fn(),
    getDocumentContent: vi.fn(),
    getDocumentStatus: vi.fn() // FIX: Added the new mock method so the tests don't crash
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [DocumentDetailComponent],
      providers: [
        provideRouter([]),
        { provide: DocumentService, useValue: mockDocumentService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ id: '15' })
            }
          }
        }
      ]
    }).compileComponents();
  });

  function createComponent() {
    fixture = TestBed.createComponent(DocumentDetailComponent);
    component = fixture.componentInstance;
  }

  it('should show the loading state initially', () => {
    mockDocumentService.getDocument.mockReturnValue(new Subject());
    createComponent();
    fixture.detectChanges();
    expect(component.isLoading()).toBe(true);
  });

  it('should show the error state if the document is not found or unauthorized', () => {
    mockDocumentService.getDocument.mockReturnValue(throwError(() => new Error('Not Found')));
    createComponent();
    fixture.detectChanges();
    expect(component.isLoading()).toBe(false);
    expect(component.hasError()).toBe(true);
  });

  it('should display document details on a successful fetch', () => {
    const mockDetail: DocumentDetail = {
      id: 15,
      originalFilename: 'confidential-nda.pdf',
      contentType: 'application/pdf',
      fileSize: 2048,
      status: 'COMPLETED',
      createdAt: '2026-08-20T10:00:00Z',
      updatedAt: '2026-08-20T10:05:00Z'
    };
    mockDocumentService.getDocument.mockReturnValue(of(mockDetail));

    // Act
    createComponent();
    fixture.detectChanges();

    // Assert
    expect(component.isLoading()).toBe(false);
    expect(component.document()?.id).toBe(15);
    expect(component.document()?.originalFilename).toBe('confidential-nda.pdf');
  });
});
