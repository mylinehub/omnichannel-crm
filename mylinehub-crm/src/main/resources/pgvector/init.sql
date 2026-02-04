-- enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Organization table is already present (you provided earlier).
-- Document master table
CREATE TABLE IF NOT EXISTS document (
  id BIGSERIAL PRIMARY KEY,
  organization_id BIGINT NOT NULL,
  original_filename VARCHAR(1024),
  file_hash VARCHAR(128) NOT NULL,
  mime_type VARCHAR(255),
  size_bytes BIGINT,
  uploader VARCHAR(255),
  is_active BOOLEAN DEFAULT TRUE,
  summary TEXT,
  created_on TIMESTAMP WITH TIME ZONE DEFAULT now(),
  last_updated_on TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_document_filehash ON document(file_hash);
CREATE INDEX IF NOT EXISTS idx_document_org ON document(organization_id);

-- Document chunk (split text)
CREATE TABLE IF NOT EXISTS document_chunk (
  id BIGSERIAL PRIMARY KEY,
  document_id BIGINT REFERENCES document(id) ON DELETE CASCADE,
  chunk_index INTEGER,
  text TEXT,
  token_count INTEGER,
  created_on TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_chunk_document ON document_chunk(document_id);

-- Embeddings table (separate)
-- Note: adjust dimension in vector(...) if you change pgvector.dim
CREATE TABLE IF NOT EXISTS embedding (
  id BIGSERIAL PRIMARY KEY,
  document_chunk_id BIGINT REFERENCES document_chunk(id) ON DELETE CASCADE,
  organization_id BIGINT NOT NULL,
  vector vector(1536),
  created_on TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Create ivfflat index for vector similarity (tune lists count for your dataset)
-- You must run: SELECT ivfflat_create_index('embedding', 'vector', 100); for some PG versions
-- Fallback to a simple index if ivfflat not available
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_class WHERE relname = 'idx_embedding_vector_ivfflat'
  ) THEN
    BEGIN
      EXECUTE 'CREATE INDEX idx_embedding_vector_ivfflat ON embedding USING ivfflat (vector) WITH (lists = 100);';
    EXCEPTION WHEN OTHERS THEN
      -- ignore if ivfflat not available; create normal index
      EXECUTE 'CREATE INDEX IF NOT EXISTS idx_embedding_vector_linear ON embedding USING gin (vector);';
    END;
  END IF;
END;
$$;

-- Transcription store
CREATE TABLE IF NOT EXISTS transcription (
  id BIGSERIAL PRIMARY KEY,
  document_id BIGINT REFERENCES document(id) ON DELETE CASCADE,
  provider VARCHAR(255),
  language VARCHAR(16),
  text TEXT,
  created_on TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Ingest errors
CREATE TABLE IF NOT EXISTS ingest_error (
  id BIGSERIAL PRIMARY KEY,
  document_id BIGINT,
  filename VARCHAR(1024),
  error_message TEXT,
  created_on TIMESTAMP WITH TIME ZONE DEFAULT now()
);
