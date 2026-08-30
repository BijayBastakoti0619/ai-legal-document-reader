import { Component, Input, Output, EventEmitter } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DocumentType, DocumentUploadResponse } from '../../models/document.models';

@Component({
  selector: 'app-document-card',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './document-card.component.html',
  styleUrl: './document-card.component.css'
})
export class DocumentCardComponent {
  // Receives the document data from the parent
  @Input({ required: true }) document!: DocumentUploadResponse;

  // Receives deleting state from DashboardComponent
  @Input()
  isDeleting = false;
  // Emits events up to the parent when a button is clicked
  @Output() view = new EventEmitter<DocumentUploadResponse>();
  @Output() download = new EventEmitter<DocumentUploadResponse>();
  @Output() delete = new EventEmitter<DocumentUploadResponse>();
  @Output() analyze = new EventEmitter<DocumentUploadResponse>();

  // Moved formatting logic here since it only relates to UI presentation
  formatFileSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} bytes`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  }

  formatDocumentType(type: DocumentType | undefined): string {
    switch (type) {
      case DocumentType.LEASE: return 'Lease';
      case DocumentType.INSURANCE: return 'Insurance';
      case DocumentType.LOAN: return 'Loan';
      default: return 'Loan';
    }
  }
}
