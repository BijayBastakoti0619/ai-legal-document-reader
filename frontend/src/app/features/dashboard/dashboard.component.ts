import {
  Component,
  inject
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
import {DocumentUploadResponse} from '../../shared/models/document.models';

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
export class DashboardComponent {

  private readonly authService =
    inject(AuthService);

  private readonly router =
    inject(Router);

  private readonly documentService =
    inject(DocumentService);

  readonly recentUploads =
    this.documentService.recentUploads;


  onLogout(): void {

    this.authService.logout()
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

    if (bytes < 1024 * 1024) {

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
      window.open('', '_blank');

    this.documentService
      .getDocumentContent(document.id)
      .subscribe({

        next: blob => {

          const pdfBlob =
            new Blob(
              [blob],
              {
                type: 'application/pdf'
              }
            );

          const url =
            URL.createObjectURL(pdfBlob);

          if (newWindow) {
            newWindow.location.href = url;
          }

          setTimeout(
            () => URL.revokeObjectURL(url),
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
      .getDocumentContent(document.id)
      .subscribe({

        next: blob => {

          const pdfBlob =
            new Blob(
              [blob],
              {
                type: 'application/pdf'
              }
            );

          const url =
            URL.createObjectURL(pdfBlob);

          const link =
            window.document.createElement('a');

          link.href = url;

          link.download =
            document.originalFilename;

          window.document.body
            .appendChild(link);

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
}
