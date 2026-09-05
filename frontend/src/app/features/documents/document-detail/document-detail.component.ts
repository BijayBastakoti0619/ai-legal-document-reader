// src/app/features/documents/document-detail/document-detail.component.ts

import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core'; // FIX: Added OnDestroy
import { ActivatedRoute } from '@angular/router';
import { DatePipe, Location } from '@angular/common';
import { Subscription, timer, finalize } from 'rxjs'; // FIX: Added RxJS imports
import { switchMap, takeWhile } from 'rxjs/operators'; // FIX: Added RxJS operators

import { DocumentService } from '../../../core/services/document.service';
import { DocumentDetail } from '../../../shared/models/document.models';

@Component({
  selector: 'app-document-detail',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './document-detail.component.html',
  styleUrl: './document-detail.component.css'
})
export class DocumentDetailComponent implements OnInit, OnDestroy {

  private readonly route = inject(ActivatedRoute);
  private readonly documentService = inject(DocumentService);
  private readonly location = inject(Location);

  readonly isLoading = signal(true);
  readonly hasError = signal(false);
  readonly errorMessage = signal('');
  readonly document = signal<DocumentDetail | null>(null);

  // FIX: New signals to track specific failure messages from Developer 1
  readonly failureMessage = signal<string | null>(null);

  // FIX: Memory management for our polling loop
  private pollingSubscription?: Subscription;

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

  // FIX: Stop polling immediately if the user navigates away
  ngOnDestroy(): void {
    this.pollingSubscription?.unsubscribe();
  }

  private loadDocumentDetails(id: number): void {
    this.isLoading.set(true);
    this.hasError.set(false);
    this.errorMessage.set('');

    this.documentService
      .getDocument(id)
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
        })
      )
      .subscribe({
        next: detail => {
          this.document.set(detail);

          // FIX: Trigger the polling logic if the document isn't finished yet
          this.startStatusPolling(id, detail.status);
        },
        error: err => {
          console.error(`Failed to load document details for ID ${id}`, err);

          if (err.status === 404) {
            this.showError('This document could not be found. It may have been deleted.');
          } else if (err.status === 401 || err.status === 403) {
            this.showError('You are not authorized to view this document.');
          } else if (err.status === 0) {
            this.showError('Unable to connect to the server. Please try again.');
          } else {
            this.showError('Unable to load the document details. Please try again.');
          }
        }
      });
  }

  // FIX: The core RxJS polling engine
  private startStatusPolling(id: number, currentStatus: string): void {
    const terminalStates = ['COMPLETED', 'FAILED', 'DELETED'];

    // If it's already done, don't even start polling
    if (terminalStates.includes(currentStatus)) {
      return;
    }

    // Poll every 3 seconds
    this.pollingSubscription = timer(3000, 3000).pipe(
      switchMap(() => this.documentService.getDocumentStatus(id)),
      takeWhile(response => !terminalStates.includes(response.status), true)
    ).subscribe({
      next: (statusResponse) => {
        // Update the main document signal so the UI reacts instantly
        this.document.update(doc => doc ? { ...doc, status: statusResponse.status } : null);

        // If it failed, save the safe failure message to display to the user
        if (statusResponse.status === 'FAILED') {
          this.failureMessage.set(statusResponse.message);
        }
      },
      error: (err) => {
        console.error('Status polling encountered an error', err);
        // Stop polling if the network completely drops
        this.pollingSubscription?.unsubscribe();
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
