import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { DocumentService } from '../../../core/services/document.service';

@Component({
  selector: 'app-document-history',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './document-history.component.html',
  styleUrl: './document-history.component.css'
})
export class DocumentHistoryComponent implements OnInit {
  private readonly documentService = inject(DocumentService);
  private readonly router = inject(Router);

  isLoading = signal(true);
  hasError = signal(false);
  documents = signal<any[]>([]);

  // --- NEW: Filter State & Computed Signal ---
  selectedFilter = signal<string>('ALL');

  filteredDocuments = computed(() => {
    const docs = this.documents();
    const filter = this.selectedFilter();

    if (filter === 'ALL') {
      return docs;
    }
    return docs.filter(doc => doc.documentType === filter);
  });

  // Pagination State
  currentPage = signal(0);
  totalPages = signal(1);

  ngOnInit(): void {
    this.loadDocuments(this.currentPage());
  }

private loadDocuments(page: number): void {
    this.isLoading.set(true);
    // FIX: Changed from 6 to 3 so it creates exactly 1 row of cards!
    this.documentService.getDocuments(page, 3).subscribe({
      next: (response) => {
        this.documents.set(response.content);
        this.totalPages.set(response.page?.totalPages || 1);
        this.currentPage.set(page);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load document history', err);
        this.hasError.set(true);
        this.isLoading.set(false);
      }
    });
  }

  // --- NEW: Filter Handler ---
  onFilterChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedFilter.set(value);
  }

  // Pagination Methods
  nextPage(): void {
    if (this.currentPage() < this.totalPages() - 1) {
      this.loadDocuments(this.currentPage() + 1);
    }
  }

  previousPage(): void {
    if (this.currentPage() > 0) {
      this.loadDocuments(this.currentPage() - 1);
    }
  }

  // Helper Methods
  formatFileSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} bytes`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  }

  formatDocumentType(type: string | undefined): string {
    switch (type) {
      case 'LEASE': return 'Lease';
      case 'INSURANCE': return 'Insurance';
      case 'LOAN': return 'Loan';
      default: return 'Loan';
    }
  }

  viewDocument(document: any): void {
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

  downloadDocument(document: any): void {
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

  viewAnalysis(document: any): void {
    this.router.navigate(['/documents', document.id, 'analysis']);
  }
}
