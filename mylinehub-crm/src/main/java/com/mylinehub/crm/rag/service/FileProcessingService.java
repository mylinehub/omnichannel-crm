package com.mylinehub.crm.rag.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.rag.model.Document;
import com.mylinehub.crm.rag.model.DocumentChunk;
import com.mylinehub.crm.rag.model.EmbeddingEntity;
import com.mylinehub.crm.rag.model.Transcription;
import com.mylinehub.crm.rag.repository.DocumentChunkRepository;
import com.mylinehub.crm.rag.repository.DocumentRepository;
import com.mylinehub.crm.rag.repository.EmbeddingRepository;
import com.mylinehub.crm.rag.repository.TranscriptionRepository;
import com.mylinehub.crm.rag.util.HashUtil;
import com.mylinehub.crm.rag.util.TikaTextExtractor;
import com.pgvector.PGvector;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FileProcessingService {

    private final TikaTextExtractor tikaTextExtractor;
    private final EmbeddingService embeddingService;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingRepository embeddingRepository;
    private final TranscriptionService transcriptionService;
    private final TranscriptionRepository transcriptionRepository;
    private final ObjectMapper objectMapper;

    @Value("${chunk.size.tokens}")
    private int chunkSizeTokens;

    @Value("${chunk.overlap.tokens}")
    private int chunkOverlapTokens;

    public FileProcessingService(TikaTextExtractor tikaTextExtractor,
                                 EmbeddingService embeddingService,
                                 DocumentRepository documentRepository,
                                 DocumentChunkRepository documentChunkRepository,
                                 EmbeddingRepository embeddingRepository,
                                 TranscriptionService transcriptionService,
                                 TranscriptionRepository transcriptionRepository,
                                 ObjectMapper objectMapper) {
    	this.objectMapper = objectMapper;
        this.tikaTextExtractor = tikaTextExtractor;
        this.embeddingService = embeddingService;
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.embeddingRepository = embeddingRepository;
        this.transcriptionService = transcriptionService;
        this.transcriptionRepository = transcriptionRepository;
    }

    @Transactional
    public List<String> ingestFile(MultipartFile multipartFile, String organization, String uploader) throws Exception {
        System.out.println("[FileProcessing] Ingest start for file: " + multipartFile.getOriginalFilename());

        // Save to temp file
        File temp = File.createTempFile("upload-", "-" + multipartFile.getOriginalFilename());
        multipartFile.transferTo(temp);
        System.out.println("[FileProcessing] Written temp file: " + temp.getAbsolutePath());

        String hash = HashUtil.sha256OfFile(temp);
        System.out.println("[FileProcessing] Computed SHA-256: " + hash);

        boolean processFile = true;

        // Check for existing document
        Optional<Document> existingOpt = documentRepository.findByOriginalFilenameAndMimeTypeAndOrganization(
                multipartFile.getOriginalFilename(),
                multipartFile.getContentType(),
                organization
        );

        Document document;
        if (existingOpt.isPresent()) {
            document = existingOpt.get();
            if (hash.equals(document.getFileHash())) {
                processFile = false;
            } else {
                System.out.println("[FileProcessing] Found existing document, updating: id=" + document.getId());
                document.setLastUpdatedOn(java.time.Instant.now());
                documentRepository.save(document);

                // Delete previous chunks and embeddings
                deleteAllPreviousChunksAndEmbeddingsForDoc(document.getId());
            }
        } else {
            document = Document.builder()
                    .organization(organization)
                    .originalFilename(multipartFile.getOriginalFilename())
                    .isActive(true)
                    .fileHash(hash)
                    .mimeType(multipartFile.getContentType())
                    .sizeBytes(multipartFile.getSize())
                    .uploader(uploader)
                    .build();
            document = documentRepository.save(document);
            System.out.println("[FileProcessing] Saved new document id: " + document.getId());
        }

        if (processFile) {
            // --- Extract text ---
            String extracted = tikaTextExtractor.extractTextFromFile(temp);
            System.out.println("[FileProcessing] Extracted text length: " + (extracted != null ? extracted.length() : 0));

            extracted = cleanExtractedText(extracted);
            System.out.println("[FileProcessing] After cleaning: " + (extracted != null ? extracted.length() : 0));

            // --- Transcription for media ---
            if (multipartFile.getContentType() != null &&
                    (multipartFile.getContentType().startsWith("audio/") || multipartFile.getContentType().startsWith("video/"))) {
                System.out.println("[FileProcessing] Media detected, scheduling transcription task...");
                String transcript = transcriptionService.transcribe(temp, multipartFile.getContentType());
                Transcription t = Transcription.builder()
                        .documentId(document.getId())
                        .provider("openai")
                        .language("unknown")
                        .text(transcript)
                        .build();
                transcriptionRepository.save(t);

                if (transcript != null && !transcript.isEmpty()) {
                    extracted = (extracted == null ? "" : extracted + "\n") + transcript;
                }
                System.out.println("[FileProcessing] Transcription saved.");
            }

            // --- Step 1: Chunk text ---
            List<String> chunks = this.embeddingService.chunkText(extracted);
            System.out.println("[FileProcessing] Chunked into " + chunks.size() + " chunks");

            if (chunks.isEmpty()) {
                throw new Exception("[FileProcessing] No chunks to process for document " + document.getId());
            }

            // --- Step 2: Get batch embeddings ---
            List<float[]> vectors = embeddingService.embedTextBatch(chunks);

            if (vectors.size() != chunks.size()) {
                throw new IllegalStateException(
                        "Embedding count mismatch: chunks=" + chunks.size() + " vectors=" + vectors.size()
                );
            }

            // --- Step 3: Prepare and save chunk entities ---
            List<DocumentChunk> chunkEntities = new ArrayList<>(chunks.size());
            for (int i = 0; i < chunks.size(); i++) {
                chunkEntities.add(
                        DocumentChunk.builder()
                                .documentId(document.getId())
                                .chunkIndex(i)
                                .text(chunks.get(i))
                                .tokenCount(approxTokensForText(chunks.get(i)))
                                .build()
                );
            }

            int batchSize = 500;
            List<DocumentChunk> savedChunks = new ArrayList<>(chunkEntities.size());

            for (int start = 0; start < chunkEntities.size(); start += batchSize) {
                int end = Math.min(start + batchSize, chunkEntities.size());
                List<DocumentChunk> batch = chunkEntities.subList(start, end);
                List<DocumentChunk> savedBatch = documentChunkRepository.saveAll(batch);
                savedChunks.addAll(savedBatch);
            }

            chunkEntities = savedChunks;

            // --- Step 4: Build and save embedding entities using PGvector ---
            List<EmbeddingEntity> embedEntities = new ArrayList<>(chunkEntities.size());
            for (int i = 0; i < chunkEntities.size(); i++) {
                DocumentChunk dc = chunkEntities.get(i);
                embedEntities.add(
                        EmbeddingEntity.builder()
                                .documentChunkId(dc.getId())
                                .organization(document.getOrganization())
                                .vector(new PGvector(vectors.get(i))) // wrap float[] as PGvector
                                .build()
                );
            }

            for (int start = 0; start < chunkEntities.size(); start += 500) {
                int end = Math.min(start + 500, chunkEntities.size());
                embeddingRepository.saveAll(embedEntities.subList(start, end));
            }
            System.out.println("[FileProcessing] Saved embeddings for document id " + document.getId());

            // --- Cleanup temp file ---
            try {
                FileUtils.forceDelete(temp);
            } catch (Exception e) {
                System.out.println("[FileProcessing] Failed to delete temp file: " + e.getMessage());
            }
        }

        return List.of(hash);
    }

    // --- Delete all previous chunks and embeddings for a document ---
    @Transactional
    public void deleteAllPreviousChunksAndEmbeddingsForDoc(Long documentId) {
        List<DocumentChunk> existingChunks = documentChunkRepository.findByDocumentIdOrderByChunkIndex(documentId);
        if (!existingChunks.isEmpty()) {
            List<Long> chunkIds = new ArrayList<>();
            for (DocumentChunk dc : existingChunks) {
                chunkIds.add(dc.getId());
            }
            embeddingRepository.deleteByDocumentChunkIds(chunkIds);
            documentChunkRepository.deleteAll(existingChunks);
            System.out.println("[FileProcessing] Deleted previous chunks and embeddings for document id " + documentId);
        }
    }

    // --- Cleaning function ---
    private String cleanExtractedText(String text) {
        if (text == null) return "";
        text = text.replaceAll("\\p{Cntrl}", " "); // remove control chars
        text = text.replaceAll("\\s+", " "); // collapse whitespace
        return text.trim();
    }

    private int approxTokensForText(String text) {
        if (text == null) return 0;
        return Math.max(1, text.length() / 4);
    }
    
}
