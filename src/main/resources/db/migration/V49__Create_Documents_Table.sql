-- V49: Create Documents Table
-- Migration to support document management system
-- Enables secure storage, versioning, and management of financial documents

CREATE TABLE IF NOT EXISTS documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL CHECK (file_size > 0),
    document_type VARCHAR(50) NOT NULL CHECK (document_type IN (
        'AGREEMENT', 'RECEIPT', 'INVOICE', 'STATEMENT', 'TAX_DOCUMENT',
        'INSURANCE_POLICY', 'LOAN_DOCUMENT', 'INVESTMENT_CERTIFICATE',
        'BANK_STATEMENT', 'PROPERTY_DEED', 'ID_PROOF', 'ADDRESS_PROOF', 'OTHER'
    )),
    category VARCHAR(50) NOT NULL CHECK (category IN (
        'PERSONAL', 'TAX', 'INVESTMENT', 'LOAN', 'INSURANCE',
        'BANKING', 'PROPERTY', 'LEGAL', 'MEDICAL', 'OTHER'
    )),
    description VARCHAR(1000),
    tags VARCHAR(500),
    related_entity_id BIGINT,
    related_entity_type VARCHAR(50),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_date TIMESTAMP,
    is_encrypted BOOLEAN DEFAULT FALSE,
    encryption_key VARCHAR(100),
    is_verified BOOLEAN DEFAULT FALSE,
    verified_at TIMESTAMP,
    verified_by VARCHAR(100),
    checksum VARCHAR(100),
    version INTEGER NOT NULL DEFAULT 1,
    previous_version_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT fk_document_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_document_previous_version FOREIGN KEY (previous_version_id) REFERENCES documents(id) ON DELETE SET NULL,
    CONSTRAINT chk_version_positive CHECK (version > 0)
);

-- Create indexes for performance optimization
CREATE INDEX idx_documents_user_id ON documents(user_id);
CREATE INDEX idx_documents_document_type ON documents(document_type);
CREATE INDEX idx_documents_category ON documents(category);
CREATE INDEX idx_documents_user_active ON documents(user_id, is_active);
CREATE INDEX idx_documents_related_entity ON documents(related_entity_id, related_entity_type);
CREATE INDEX idx_documents_expiry_date ON documents(expiry_date);
CREATE INDEX idx_documents_file_name ON documents(file_name);
CREATE INDEX idx_documents_tags ON documents(tags);
CREATE INDEX idx_documents_uploaded_at ON documents(uploaded_at);
CREATE INDEX idx_documents_verified ON documents(is_verified);
