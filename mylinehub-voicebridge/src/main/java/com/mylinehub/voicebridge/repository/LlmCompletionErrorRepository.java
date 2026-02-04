package com.mylinehub.voicebridge.repository;

import com.mylinehub.voicebridge.models.LlmCompletionError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LlmCompletionErrorRepository extends JpaRepository<LlmCompletionError, Long> {
}
