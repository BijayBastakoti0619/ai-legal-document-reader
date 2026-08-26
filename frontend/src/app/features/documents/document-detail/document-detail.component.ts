// src/app/features/documents/document-detail/document-detail.component.ts

import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { DocumentService } from '../../../core/services/document.service';
import { DocumentDetail } from '../../../shared/models/document.models';

@Component({
  selector: 'app-document-detail',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './document-detail.component.html',
  styleUrl: './document-detail.component.css'
})
export class DocumentDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly documentService = inject(DocumentService);

  // Initialize strictly to pass the "should show loading state initially" test
  isLoading = signal(true);
  hasError = signal(false);
  document = signal<DocumentDetail | null>(null);

  ngOnInit(): void {
    // 1. Extract the 'id' parameter from the URL (e.g., /documents/15)
    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam) {
      this.loadDocumentDetails(Number(idParam));
    } else {
      // If there is no ID in the URL, instantly fail
      this.hasError.set(true);
      this.isLoading.set(false);
    }
  }

  private loadDocumentDetails(id: number): void {
    // 2. Call our service to fetch the detailed metadata
    this.documentService.getDocument(id).subscribe({
      next: (detail) => {
        this.document.set(detail);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error(`Failed to load document details for ID ${id}`, err);
        this.hasError.set(true);
        this.isLoading.set(false);
      }
    });
  }
}
