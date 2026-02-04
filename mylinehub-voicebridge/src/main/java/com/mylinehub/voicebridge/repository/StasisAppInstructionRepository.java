/*
 * File: src/main/java/com/mylinehub/voicebridge/config/StasisAppInstructionRepository.java
 */
package com.mylinehub.voicebridge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.models.StasisAppInstruction;

import java.util.List;
import java.util.Optional;

public interface StasisAppInstructionRepository extends JpaRepository<StasisAppInstruction, String> {

  List<StasisAppInstruction> findByActiveTrue();

  @Query("select c from StasisAppInstruction c where c.stasis_app_name = :name and c.active = true")
  Optional<StasisAppConfig> findActiveByName(@Param("name") String name);
  
}
