// src/app/shared/models/document.models.ts

export type DocumentStatus =
  | 'UPLOADED'
  | 'EXTRACTING'
  | 'ANALYZING'
  | 'COMPLETED'
  | 'FAILED'
  | 'DELETED';

export enum DocumentType {
  LEASE = 'LEASE',
  INSURANCE = 'INSURANCE',
  LOAN = 'LOAN'
}

export interface DocumentUploadResponse {
  id: number;
  originalFilename: string;
  contentType: string;
  fileSize: number;
  documentType: DocumentType;
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

// NEW: Added the new status polling response model
export interface DocumentStatusResponse {
  documentId: number;
  status: DocumentStatus;
  failureCode: string | null;
  message: string | null;
}
