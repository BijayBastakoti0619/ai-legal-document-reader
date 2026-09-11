ALTER TABLE documents
DROP CONSTRAINT IF EXISTS chk_documents_status;

ALTER TABLE documents
    ADD CONSTRAINT chk_documents_status
        CHECK (
            status IN (
                       'UPLOADED',
                       'EXTRACTING',
                       'EXTRACTED',
                       'ANALYZING',
                       'COMPLETED',
                       'FAILED',
                       'DELETED'
                )
            );