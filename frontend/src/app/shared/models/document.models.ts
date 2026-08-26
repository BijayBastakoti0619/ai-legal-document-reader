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
