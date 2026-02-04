package com.mylinehub.crm.rag.repository;

import com.mylinehub.crm.rag.model.Transcription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranscriptionRepository extends JpaRepository<Transcription, Long> {
}
