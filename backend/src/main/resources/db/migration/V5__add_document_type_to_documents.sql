ALTER TABLE documents
    ADD COLUMN IF NOT EXISTS document_type VARCHAR(30);

UPDATE documents
SET document_type = 'LOAN'
WHERE document_type IS NULL
   OR document_type NOT IN (
                            'LEASE',
                            'INSURANCE',
                            'LOAN'
    );

ALTER TABLE documents
    ALTER COLUMN document_type SET NOT NULL;

ALTER TABLE documents
DROP CONSTRAINT IF EXISTS chk_documents_document_type;

ALTER TABLE documents
    ADD CONSTRAINT chk_documents_document_type
        CHECK (
            document_type IN (
                              'LEASE',
                              'INSURANCE',
                              'LOAN'
                )
            );