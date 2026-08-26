// src/app/shared/models/document.models.ts

export type DocumentStatus =
  | 'UPLOADED'
  | 'EXTRACTING'
  | 'ANALYZING'
  | 'COMPLETED'
  | 'FAILED'
  | 'DELETED';

export interface DocumentUploadResponse {
  id: number;
  originalFilename: string;
  contentType: string;
  fileSize: number;
  status: DocumentStatus;
  createdAt: string;
}

// NEW: For the paginated history table
export interface DocumentSummary {
  id: number;
  originalFilename: string;
  fileSize: number;
  status: DocumentStatus;
  createdAt: string;
}

// NEW: For the specific document details page
export interface DocumentDetail {
  id: number;
  originalFilename: string;
  contentType: string;
  fileSize: number;
  status: DocumentStatus;
  createdAt: string;
  updatedAt: string;
}

// NEW: A generic wrapper matching Spring Boot's Page<T> JSON structure
export interface PaginatedResponse<T> {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}
