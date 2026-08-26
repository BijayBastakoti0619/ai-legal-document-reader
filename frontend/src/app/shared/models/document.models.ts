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
