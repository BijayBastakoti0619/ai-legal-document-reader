import {
  Component,
  OnInit,
  inject,
  signal,
  computed
} from '@angular/core';

import { RouterLink, Router } from '@angular/router';
import { finalize } from 'rxjs';

import { DocumentService } from '../../../core/services/document.service';
import { DocumentCardComponent } from '../../../shared/components/document-card/document-card.component';

@Component({
  selector: 'app-document-history',
  standalone: true,
  imports: [
    RouterLink,
    DocumentCardComponent
  ],
  templateUrl: './document-history.component.html',
  styleUrl: './document-history.component.css'
})
export class DocumentHistoryComponent implements OnInit {

  private readonly documentService = inject(DocumentService);
  private readonly router = inject(Router);

  // Page loading state
  readonly isLoading = signal(true);

  readonly hasError = signal(false);

  // Documents on current page
  readonly documents = signal<any[]>([]);

  // Current filter
  readonly selectedFilter = signal<string>('ALL');

  // ID of document currently being deleted
  readonly deletingDocumentId = signal<number | null>(null);


  readonly filteredDocuments = computed(() => {

    const docs = this.documents();
    const filter = this.selectedFilter();

    if (filter === 'ALL') {
      return docs;
    }

    return docs.filter(
      doc => doc.documentType === filter
    );
  });


  // Pagination
  readonly currentPage = signal(0);

  readonly totalPages = signal(1);


  ngOnInit(): void {
    this.loadDocuments(this.currentPage());
  }


  private loadDocuments(page: number): void {

    this.isLoading.set(true);
    this.hasError.set(false);

    // Fetch 2 documents per page
    this.documentService
      .getDocuments(page, 2)
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
        })
      )
      .subscribe({

        next: response => {

          this.documents.set(response.content);

          this.totalPages.set(
            response.page?.totalPages || 1
          );

          this.currentPage.set(page);
        },

        error: err => {

          console.error(
            'Failed to load document history',
            err
          );

          this.hasError.set(true);
        }
      });
  }


  onFilterChange(event: Event): void {

    const value =
      (event.target as HTMLSelectElement).value;

    this.selectedFilter.set(value);
  }


  nextPage(): void {

    if (
      this.currentPage() <
      this.totalPages() - 1
    ) {

      this.loadDocuments(
        this.currentPage() + 1
      );
    }
  }


  previousPage(): void {

    if (this.currentPage() > 0) {

      this.loadDocuments(
        this.currentPage() - 1
      );
    }
  }


  viewDocument(document: any): void {

    const newWindow =
      window.open('', '_blank');

    this.documentService
      .getDocumentContent(document.id)
      .subscribe({

        next: blob => {

          const pdfBlob = new Blob(
            [blob],
            { type: 'application/pdf' }
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


  downloadDocument(document: any): void {

    this.documentService
      .getDocumentContent(document.id)
      .subscribe({

        next: blob => {

          const pdfBlob = new Blob(
            [blob],
            { type: 'application/pdf' }
          );

          const url =
            URL.createObjectURL(pdfBlob);

          const link =
            window.document.createElement('a');

          link.href = url;

          link.download =
            document.originalFilename;

          window.document.body.appendChild(link);

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


  viewAnalysis(document: any): void {

    this.router.navigate([
      '/documents',
      document.id,
      'analysis'
    ]);
  }


  deleteDocument(document: any): void {

    // Prevent another delete while one is already running
    if (this.deletingDocumentId() !== null) {
      return;
    }

    const confirmed = window.confirm(
      `Are you sure you want to delete "${document.originalFilename}"?`
    );

    if (!confirmed) {
      return;
    }

    // Show loader only on selected card
    this.deletingDocumentId.set(document.id);

    this.documentService
      .deleteDocument(document.id)
      .pipe(
        finalize(() => {

          // Remove deleting loader whether success or failure
          this.deletingDocumentId.set(null);

        })
      )
      .subscribe({

        next: () => {

          /*
           * If this is the last document on the page,
           * move back one page when possible.
           */
          if (
            this.documents().length === 1 &&
            this.currentPage() > 0
          ) {

            this.currentPage.update(
              page => page - 1
            );
          }

          /*
           * Reload documents from backend.
           * This also allows another document to
           * slide into the empty grid position.
           */
          this.loadDocuments(
            this.currentPage()
          );
        },

        error: error => {

          console.error(
            'Unable to delete document',
            error
          );

          alert(
            'The document could not be deleted. Please try again.'
          );
        }
      });
  }
}
