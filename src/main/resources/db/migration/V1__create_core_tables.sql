-- Folders table
CREATE TABLE folders (
    id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    parent_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    is_archived BOOLEAN DEFAULT FALSE NOT NULL,
    archived_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_folders PRIMARY KEY (id)
);

CREATE INDEX idx_folders_user_id ON folders(user_id);
CREATE INDEX idx_folders_user_parent ON folders(user_id, parent_id);
CREATE INDEX idx_folders_user_archived_deleted ON folders(user_id, is_archived, is_deleted);
CREATE INDEX idx_folders_user_updated ON folders(user_id, updated_at);
CREATE INDEX idx_folders_user_created ON folders(user_id, created_at);

-- Files table
CREATE TABLE files (
    id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(255) NOT NULL,
    mime_type VARCHAR(255) NOT NULL,
    size_byte NUMERIC(10,2),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_files PRIMARY KEY (id)
);

CREATE INDEX idx_files_id ON files(id);

-- Log entries table
CREATE TABLE log_entries (
    id UUID NOT NULL,
    author_id VARCHAR(255) NOT NULL,
    audio_file_id UUID,
    audio_url VARCHAR(500),
    processing_status VARCHAR(50) DEFAULT 'pending' NOT NULL,
    transcript TEXT,
    structured_summary JSONB,
    summary_text TEXT,
    title VARCHAR(255),
    category VARCHAR(50) DEFAULT 'other' NOT NULL,
    duration_seconds NUMERIC(10,3),
    folder_id UUID,
    transcription_error TEXT,
    enrichment_error TEXT,
    archived BOOLEAN DEFAULT FALSE NOT NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_log_entries PRIMARY KEY (id)
);

CREATE INDEX idx_log_entries_author_id ON log_entries(author_id);
CREATE INDEX idx_log_entries_author_created ON log_entries(author_id, created_at);
CREATE INDEX idx_log_entries_author_updated ON log_entries(author_id, updated_at);
CREATE INDEX idx_log_entries_author_processing_status ON log_entries(author_id, processing_status);
CREATE INDEX idx_log_entries_author_category ON log_entries(author_id, category);
CREATE INDEX idx_log_entries_author_deleted ON log_entries(author_id, deleted_at);

-- Log entry artifacts table
CREATE TABLE log_entry_artifacts (
    id UUID NOT NULL,
    log_entry_id UUID NOT NULL,
    author_id VARCHAR(255) NOT NULL,
    type VARCHAR(40) DEFAULT 'photo' NOT NULL,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    captured_at_offset_seconds NUMERIC(10,3) DEFAULT 0 NOT NULL,
    server_file_id UUID NOT NULL,
    duration_seconds NUMERIC(10,3),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_log_entry_artifacts PRIMARY KEY (id)
);

CREATE INDEX idx_log_entry_artifacts_entry_captured ON log_entry_artifacts(log_entry_id, captured_at);
CREATE INDEX idx_log_entry_artifacts_entry_created ON log_entry_artifacts(log_entry_id, created_at);
CREATE INDEX idx_log_entry_artifacts_entry_type ON log_entry_artifacts(log_entry_id, type);
CREATE INDEX idx_log_entry_artifacts_author ON log_entry_artifacts(author_id, id);

-- Entity changed messages table
CREATE TABLE entity_changed_messages (
    id UUID NOT NULL,
    entity_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    changed_by_user_id VARCHAR(255) NOT NULL,
    receiver_user_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_entity_changed_messages PRIMARY KEY (id)
);

CREATE INDEX idx_entity_changed_messages_receiver_user_id ON entity_changed_messages(receiver_user_id);
CREATE INDEX idx_entity_changed_messages_receiver_status ON entity_changed_messages(receiver_user_id, status);
CREATE INDEX idx_entity_changed_messages_entity ON entity_changed_messages(entity_id, entity_type);
CREATE INDEX idx_entity_changed_messages_created_at ON entity_changed_messages(created_at);
CREATE INDEX idx_entity_changed_messages_changed_by ON entity_changed_messages(changed_by_user_id);
