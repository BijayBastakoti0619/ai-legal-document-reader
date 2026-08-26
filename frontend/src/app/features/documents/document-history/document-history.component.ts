// src/app/features/documents/document-history/document-history.component.ts

import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DocumentService } from '../../../core/services/document.service';
import { DocumentSummary } from '../../../shared/models/document.models';

@Component({
  selector: 'app-document-history',
  standalone: true,
  imports: [DatePipe, RouterLink], // Required for formatting dates and the "New Upload" link
  templateUrl: './document-history.component.html',
  styleUrl: './document-history.component.css'
})
export class DocumentHistoryComponent implements OnInit {
  private readonly documentService = inject(DocumentService);

  // Initialize strictly to pass the "should show loading state initially" test
  isLoading = signal(true);
  hasError = signal(false);
  documents = signal<DocumentSummary[]>([]);

  ngOnInit(): void {
    this.loadDocuments();
  }

  private loadDocuments(): void {
    // 1. Fetch page 0, size 10 from the Spring Boot API
    this.documentService.getDocuments(0, 10).subscribe({
      next: (response) => {
        this.documents.set(response.content);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load document history', err);
        this.hasError.set(true);
        this.isLoading.set(false);
      }
    });
  }

  // --- RESTORED HELPER METHODS (From your friend's original dashboard) ---

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} bytes`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  }

  viewDocument(document: DocumentSummary): void {
    const newWindow = window.open('', '_blank');
    this.documentService.getDocumentContent(document.id).subscribe({
      next: (blob) => {
        const pdfBlob = new Blob([blob], { type: 'application/pdf' });
        const url = URL.createObjectURL(pdfBlob);
        if (newWindow) newWindow.location.href = url;
        setTimeout(() => URL.revokeObjectURL(url), 60_000);
      },
      error: (error) => {
        console.error('Unable to open PDF', error);
        if (newWindow) newWindow.close();
        alert('The PDF could not be opened.');
      }
    });
  }

  downloadDocument(document: DocumentSummary): void {
    this.documentService.getDocumentContent(document.id).subscribe({
      next: (blob) => {
        const pdfBlob = new Blob([blob], { type: 'application/pdf' });
        const url = URL.createObjectURL(pdfBlob);
        const link = window.document.createElement('a');
        link.href = url;
        link.download = document.originalFilename;
        window.document.body.appendChild(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Unable to download PDF', error);
        alert('The PDF could not be downloaded.');
      }
    });
  }
}
