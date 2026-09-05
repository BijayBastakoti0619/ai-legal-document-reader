-- Failure tracking columns for the document processing lifecycle
ALTER TABLE documents ADD COLUMN failure_code VARCHAR(50);
ALTER TABLE documents ADD COLUMN failure_message VARCHAR(1000);