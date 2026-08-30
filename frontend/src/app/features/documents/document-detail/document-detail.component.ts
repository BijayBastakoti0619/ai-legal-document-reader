// src/app/features/documents/document-detail/document-detail.component.ts

import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DatePipe, Location } from '@angular/common'; // FIX: Imported Location
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
  private readonly location = inject(Location); // FIX: Injected Location service

  isLoading = signal(true);
  hasError = signal(false);
  document = signal<DocumentDetail | null>(null);

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam) {
      this.loadDocumentDetails(Number(idParam));
    } else {
      this.hasError.set(true);
      this.isLoading.set(false);
    }
  }

  private loadDocumentDetails(id: number): void {
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

  // FIX: Added method to dynamically route to the previous page
  goBack(): void {
    this.location.back();
  }
}
