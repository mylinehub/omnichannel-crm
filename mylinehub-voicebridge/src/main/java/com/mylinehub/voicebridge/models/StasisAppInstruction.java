/*
 * File: src/main/java/com/mylinehub/voicebridge/config/StasisAppInstruction.java
 *
 * Purpose:
 *   - Stores the long AI system prompt / instructions per Stasis app.
 *   - One row per stasis_app_name.
 */

package com.mylinehub.voicebridge.models;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "stasis_app_instruction")
public class StasisAppInstruction {

  @Id
  @Column(name = "stasis_app_name", nullable = false, length = 128)
  private String stasis_app_name;

  @Column(name = "active", nullable = false)
  private boolean active = true;

  //Store as TEXT in Postgres (NOT oid)
  @JdbcTypeCode(SqlTypes.LONGVARCHAR)
  @Column(name = "instructions", nullable = false, columnDefinition = "text")
  private String instructions;
  
  @JdbcTypeCode(SqlTypes.LONGVARCHAR)
  @Column(name = "completion_instructions", columnDefinition = "text")
  private String completionInstructions;
  
  @Column(name = "fetch_customer_info", nullable = false)
  private boolean fetchCustomerInfo;
 
}
