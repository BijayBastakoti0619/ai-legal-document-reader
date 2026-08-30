import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { DocumentService } from '../../core/services/document.service';
import { DocumentUploadResponse } from '../../shared/models/document.models';

import { DocumentCardComponent } from '../../shared/components/document-card/document-card.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    RouterLink,
    DocumentCardComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly documentService = inject(DocumentService);

  readonly recentUploads = signal<DocumentUploadResponse[]>([]);

  // Stores the ID of the document currently being deleted
  readonly deletingDocumentId = signal<number | null>(null);

  ngOnInit(): void {
    this.documentService.getDocuments(0, 6).subscribe({
      next: response => {
        const activeDocuments = response.content.filter(
          (document: DocumentUploadResponse) =>
            document.status !== 'DELETED'
        );

        this.recentUploads.set(activeDocuments);
      },

      error: err => {
        console.error(
          'Failed to load recent documents from database',
          err
        );
      }
    });
  }

  onLogout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },

      error: err => {
        console.error(
          'Logout failed on the server, forcing local logout',
          err
        );

        this.router.navigate(['/login']);
      }
    });
  }

  viewDocument(document: DocumentUploadResponse): void {
    const newWindow = window.open('', '_blank');

    this.documentService
      .getDocumentContent(document.id)
      .subscribe({
        next: blob => {
          const pdfBlob = new Blob(
            [blob],
            { type: 'application/pdf' }
          );

          const url = URL.createObjectURL(pdfBlob);

          if (newWindow) {
            newWindow.location.href = url;
          }

          setTimeout(
            () => URL.revokeObjectURL(url),
            60_000
          );
        },

        error: error => {
          console.error('Unable to open PDF', error);

          if (newWindow) {
            newWindow.close();
          }

          alert('The PDF could not be opened.');
        }
      });
  }

  downloadDocument(document: DocumentUploadResponse): void {
    this.documentService
      .getDocumentContent(document.id)
      .subscribe({
        next: blob => {
          const pdfBlob = new Blob(
            [blob],
            { type: 'application/pdf' }
          );

          const url = URL.createObjectURL(pdfBlob);

          const link = window.document.createElement('a');

          link.href = url;
          link.download = document.originalFilename;

          window.document.body.appendChild(link);

          link.click();
          link.remove();

          URL.revokeObjectURL(url);
        },

        error: error => {
          console.error(
            'Unable to download PDF',
            error
          );

          alert(
            'The PDF could not be downloaded.'
          );
        }
      });
  }

  viewAnalysis(document: DocumentUploadResponse): void {
    this.router.navigate([
      '/documents',
      document.id,
      'analysis'
    ]);
  }

  deleteDocument(document: DocumentUploadResponse): void {

    // Prevent another delete while one is already running
    if (this.deletingDocumentId() !== null) {
      return;
    }

    const confirmed = window.confirm(
      `Are you sure you want to delete "${document.originalFilename}"?`
    );

    if (!confirmed) {
      return;
    }

    // Mark only this card as deleting
    this.deletingDocumentId.set(document.id);

    this.documentService
      .deleteDocument(document.id)
      .pipe(
        finalize(() => {
          // Remove loader whether delete succeeds or fails
          this.deletingDocumentId.set(null);
        })
      )
      .subscribe({
        next: () => {

          // Remove deleted card from dashboard
          this.recentUploads.update(documents =>
            documents.filter(
              item => item.id !== document.id
            )
          );
        },

        error: error => {
          console.error(
            'Unable to delete document',
            error
          );

          alert(
            'The document could not be deleted. Please try again.'
          );
        }
      });
  }
}
