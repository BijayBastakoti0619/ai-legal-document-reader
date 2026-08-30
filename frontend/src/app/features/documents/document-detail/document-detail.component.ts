// src/app/features/documents/document-detail/document-detail.component.ts

import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DatePipe, Location } from '@angular/common';
import { finalize } from 'rxjs';

import { DocumentService } from '../../../core/services/document.service';
import { DocumentDetail } from '../../../shared/models/document.models';

@Component({
  selector: 'app-document-detail',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './document-detail.component.html',
  styleUrl: './document-detail.component.css'
})
export class DocumentDetailComponent implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly documentService = inject(DocumentService);
  private readonly location = inject(Location);

  // Loading state
  readonly isLoading = signal(true);

  // Error state
  readonly hasError = signal(false);

  // User-friendly error message
  readonly errorMessage = signal('');

  // Document data
  readonly document = signal<DocumentDetail | null>(null);

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (!idParam) {
      this.showError('Invalid document ID.');
      return;
    }

    const documentId = Number(idParam);

    if (Number.isNaN(documentId)) {
      this.showError('Invalid document ID.');
      return;
    }

    this.loadDocumentDetails(documentId);
  }

  private loadDocumentDetails(id: number): void {

    // Reset state before request
    this.isLoading.set(true);
    this.hasError.set(false);
    this.errorMessage.set('');

    this.documentService
      .getDocument(id)
      .pipe(
        finalize(() => {
          // Loader always stops on success or error
          this.isLoading.set(false);
        })
      )
      .subscribe({

        next: detail => {
          this.document.set(detail);
        },

        error: err => {
          console.error(
            `Failed to load document details for ID ${id}`,
            err
          );

          if (err.status === 404) {
            this.showError(
              'This document could not be found. It may have been deleted.'
            );
          } else if (err.status === 401 || err.status === 403) {
            this.showError(
              'You are not authorized to view this document.'
            );
          } else if (err.status === 0) {
            this.showError(
              'Unable to connect to the server. Please try again.'
            );
          } else {
            this.showError(
              'Unable to load the document details. Please try again.'
            );
          }
        }
      });
  }

  private showError(message: string): void {
    this.hasError.set(true);
    this.errorMessage.set(message);
    this.isLoading.set(false);
  }

  goBack(): void {
    this.location.back();
  }
}
