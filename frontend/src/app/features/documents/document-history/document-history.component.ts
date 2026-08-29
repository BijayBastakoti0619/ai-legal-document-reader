import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { DocumentService } from '../../../core/services/document.service';
import { DocumentCardComponent } from '../../../shared/components/document-card/document-card.component';

@Component({
  selector: 'app-document-history',
  standalone: true,
  imports: [RouterLink, DocumentCardComponent],
  templateUrl: './document-history.component.html',
  styleUrl: './document-history.component.css'
})
export class DocumentHistoryComponent implements OnInit {
  private readonly documentService = inject(DocumentService);
  private readonly router = inject(Router);

  isLoading = signal(true);
  hasError = signal(false);

  documents = signal<any[]>([]);
  selectedFilter = signal<string>('ALL');

  filteredDocuments = computed(() => {
    const docs = this.documents();
    const filter = this.selectedFilter();
    if (filter === 'ALL') return docs;
    return docs.filter(doc => doc.documentType === filter);
  });

  currentPage = signal(0);
  totalPages = signal(1);

  ngOnInit(): void {
    this.loadDocuments(this.currentPage());
  }

  private loadDocuments(page: number): void {
    this.isLoading.set(true);
    // Fetch exactly 2 items per page directly from our newly updated backend
    this.documentService.getDocuments(page, 2).subscribe({
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

  onFilterChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedFilter.set(value);
  }

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

  deleteDocument(document: any): void {
    const confirmed = window.confirm(`Are you sure you want to delete "${document.originalFilename}"?`);
    if (!confirmed) return;

    this.documentService.deleteDocument(document.id).subscribe({
      next: () => {
        // Safety: If deleting the last item on a page, bounce back one page
        if (this.documents().length === 1 && this.currentPage() > 0) {
          this.currentPage.update(p => p - 1);
        }
        // Force the backend to fetch fresh data so the next item slides into the empty grid slot
        this.loadDocuments(this.currentPage());
      },
      error: error => {
        console.error('Unable to delete document', error);
        alert('The document could not be deleted.');
      }
    });
  }
}
