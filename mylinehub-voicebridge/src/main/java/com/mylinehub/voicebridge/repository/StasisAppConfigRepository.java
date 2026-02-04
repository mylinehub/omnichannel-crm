/*
 * File: src/main/java/com/mylinehub/voicebridge/config/StasisAppConfigRepository.java
 */
package com.mylinehub.voicebridge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mylinehub.voicebridge.models.StasisAppConfig;

import java.util.List;
import java.util.Optional;

public interface StasisAppConfigRepository extends JpaRepository<StasisAppConfig, String> {

  List<StasisAppConfig> findByActiveTrue();

  @Query("select c from StasisAppConfig c where c.stasis_app_name = :name and c.active = true")
  Optional<StasisAppConfig> findActiveByName(@Param("name") String name);
  
}
