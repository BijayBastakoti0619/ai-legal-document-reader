import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';

import {
  Router,
  RouterLink
} from '@angular/router';

import { DatePipe } from '@angular/common';

import {
  AuthService
} from '../../core/services/auth.service';

import {
  DocumentService
} from '../../core/services/document.service';

import {
  DocumentType,
  DocumentUploadResponse
} from '../../shared/models/document.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  private readonly authService =
    inject(AuthService);

  private readonly router =
    inject(Router);

  private readonly documentService =
    inject(DocumentService);

  // Holds documents loaded from the backend.
  // DELETED documents are not displayed.
  readonly recentUploads =
    signal<DocumentUploadResponse[]>([]);

  ngOnInit(): void {

    this.documentService
      .getDocuments(0, 6)
      .subscribe({

        next: response => {

          const activeDocuments =
            response.content.filter(
              (document: DocumentUploadResponse) =>
                document.status !== 'DELETED'
            );

          this.recentUploads.set(
            activeDocuments
          );
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

    this.authService
      .logout()
      .subscribe({

        next: () => {

          this.router.navigate([
            '/login'
          ]);
        },

        error: err => {

          console.error(
            'Logout failed on the server, forcing local logout',
            err
          );

          this.router.navigate([
            '/login'
          ]);
        }
      });
  }

  formatFileSize(
    bytes: number
  ): string {

    if (bytes < 1024) {

      return `${bytes} bytes`;
    }

    if (
      bytes <
      1024 * 1024
    ) {

      return `${(
  bytes / 1024
).toFixed(1)} KB`;
    }

    return `${(
  bytes /
  (1024 * 1024)
).toFixed(2)} MB`;
  }

  viewDocument(
    document: DocumentUploadResponse
  ): void {

    const newWindow =
      window.open(
        '',
        '_blank'
      );

    this.documentService
      .getDocumentContent(
        document.id
      )
      .subscribe({

        next: blob => {

          const pdfBlob =
            new Blob(
              [blob],
              {
                type:
                  'application/pdf'
              }
            );

          const url =
            URL.createObjectURL(
              pdfBlob
            );

          if (newWindow) {

            newWindow.location.href =
              url;
          }

          setTimeout(
            () =>
              URL.revokeObjectURL(
                url
              ),
            60_000
          );
        },

        error: error => {

          console.error(
            'Unable to open PDF',
            error
          );

          if (newWindow) {

            newWindow.close();
          }

          alert(
            'The PDF could not be opened.'
          );
        }
      });
  }

  downloadDocument(
    document: DocumentUploadResponse
  ): void {

    this.documentService
      .getDocumentContent(
        document.id
      )
      .subscribe({

        next: blob => {

          const pdfBlob =
            new Blob(
              [blob],
              {
                type:
                  'application/pdf'
              }
            );

          const url =
            URL.createObjectURL(
              pdfBlob
            );

          const link =
            window.document
              .createElement('a');

          link.href = url;

          link.download =
            document.originalFilename;

          window.document.body
            .appendChild(link);

          link.click();

          link.remove();

          URL.revokeObjectURL(
            url
          );
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

  formatDocumentType(
    type: DocumentType | undefined
  ): string {

    switch (type) {

      case DocumentType.LEASE:
        return 'Lease';

      case DocumentType.INSURANCE:
        return 'Insurance';

      case DocumentType.LOAN:
        return 'Loan';

      default:
        return 'Loan';
    }
  }

  viewAnalysis(
    document: DocumentUploadResponse
  ): void {

    this.router.navigate([
      '/documents',
      document.id,
      'analysis'
    ]);
  }

  deleteDocument(
    document: DocumentUploadResponse
  ): void {

    const confirmed =
      window.confirm(
        `Are you sure you want to delete "${document.originalFilename}"?`
      );

    if (!confirmed) {
      return;
    }

    this.documentService
      .deleteDocument(
        document.id
      )
      .subscribe({

        next: () => {

          // Remove the document from the dashboard
          // immediately after the backend returns 204.
          this.recentUploads.update(
            documents =>
              documents.filter(
                item =>
                  item.id !==
                  document.id
              )
          );
        },

        error: error => {

          console.error(
            'Unable to delete document',
            error
          );

          alert(
            'The document could not be deleted.'
          );
        }
      });
  }
}

