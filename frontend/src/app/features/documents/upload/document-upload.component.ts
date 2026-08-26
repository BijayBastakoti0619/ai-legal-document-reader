import {
  Component,
  inject,
  signal
} from '@angular/core';

import {
  HttpErrorResponse,
  HttpEventType
} from '@angular/common/http';
import { Router } from '@angular/router';

import { finalize } from 'rxjs';

import {
  DocumentService
} from '../../../core/services/document.service';

import {
  DocumentType,
  DocumentUploadResponse
} from '../../../shared/models/document.models';

@Component({
  selector: 'app-document-upload',
  standalone: true,
  templateUrl: './document-upload.component.html',
  styleUrl: './document-upload.component.css'
})
export class DocumentUploadComponent {

  private readonly documentService =
    inject(DocumentService);
  private readonly router = inject(Router);

  private readonly maxFileSizeBytes =
    10 * 1024 * 1024;


  selectedFile =
    signal<File | null>(null);

  isUploading =
    signal(false);

  selectedDocumentType =
    signal<DocumentType | ''>('');

  uploadProgress =
    signal(0);

  errorMessage =
    signal('');

  successMessage =
    signal('');

  uploadedDocument =
    signal<DocumentUploadResponse | null>(null);


  onFileSelected(event: Event): void {

    this.resetMessages();

    const input =
      event.target as HTMLInputElement;

    const file =
      input.files?.[0];

    if (!file) {
      this.selectedFile.set(null);
      return;
    }


    if (
      !file.name
        .toLowerCase()
        .endsWith('.pdf')
    ) {

      this.selectedFile.set(null);

      this.errorMessage.set(
        'Please select a PDF file.'
      );

      input.value = '';

      return;
    }


    if (
      file.type !== 'application/pdf'
    ) {

      this.selectedFile.set(null);

      this.errorMessage.set(
        'The selected file must be a PDF.'
      );

      input.value = '';

      return;
    }


    if (
      file.size >
      this.maxFileSizeBytes
    ) {

      this.selectedFile.set(null);

      this.errorMessage.set(
        'The PDF must be 10 MB or smaller.'
      );

      input.value = '';

      return;
    }


    if (file.size === 0) {

      this.selectedFile.set(null);

      this.errorMessage.set(
        'The selected PDF is empty.'
      );

      input.value = '';

      return;
    }


    this.selectedFile.set(file);
  }


  uploadDocument(): void {

    const file =
      this.selectedFile();
    const documentType =
      this.selectedDocumentType();

    if (!file) {

      this.errorMessage.set(
        'Please select a PDF file first.'
      );

      return;


    }
    if (!documentType) {

      this.errorMessage.set(
        'Please select a document type.'
      );

      return;
    }

    this.resetMessages();

    this.uploadedDocument.set(null);

    this.isUploading.set(true);

    this.uploadProgress.set(0);


    this.documentService
      .uploadDocument(file,documentType)
      .pipe(

        finalize(() => {

          this.isUploading.set(false);

        })
      )
      .subscribe({

        next: event => {

          console.log(
            'HTTP EVENT:',
            event
          );


          if (
            event.type ===
            HttpEventType.UploadProgress
          ) {

            if (event.total) {

              const percentage =
                Math.round(
                  (
                    event.loaded /
                    event.total
                  ) * 100
                );

              this.uploadProgress.set(
                percentage
              );

            }
          }


          if (
            event.type ===
            HttpEventType.Response
          ) {

            this.uploadProgress.set(100);

            this.uploadedDocument.set(
              event.body
            );

            this.successMessage.set(
              'Document uploaded successfully.'
            );
            this.router.navigate(['/dashboard'])
          }

        },


        error: (
          error: HttpErrorResponse
        ) => {

          this.uploadProgress.set(0);

          this.handleUploadError(
            error
          );
        }

      });
  }


  clearSelection(
    fileInput: HTMLInputElement
  ): void {

    this.selectedFile.set(null);

    this.uploadedDocument.set(null);

    this.uploadProgress.set(0);

    this.resetMessages();

    fileInput.value = '';
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


  private handleUploadError(
    error: HttpErrorResponse
  ): void {

    const backendMessage =
      error.error?.message;

    if (backendMessage) {

      this.errorMessage.set(
        backendMessage
      );

      return;
    }


    if (error.status === 0) {

      this.errorMessage.set(
        'Unable to connect to the server.'
      );

      return;
    }


    if (error.status === 401) {

      this.errorMessage.set(
        'Your session has expired. Please log in again.'
      );

      return;
    }


    this.errorMessage.set(
      'The document could not be uploaded.'
    );
  }


  private resetMessages(): void {

    this.errorMessage.set('');

    this.successMessage.set('');
  }

  onDocumentTypeChange(event: Event): void {

    const select =
      event.target as HTMLSelectElement;

    this.selectedDocumentType.set(
      select.value as DocumentType | ''
    );
  }
}
