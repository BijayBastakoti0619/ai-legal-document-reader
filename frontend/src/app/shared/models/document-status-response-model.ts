export interface DocumentStatusResponse {
  documentId: number;
  status: string;
  failureCode: string | null;
  message: string | null;
}
