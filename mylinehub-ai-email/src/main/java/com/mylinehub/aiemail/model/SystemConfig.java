package com.mylinehub.aiemail.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Global system configuration, stored in database.
 *
 * Example keys:
 *  - OPENAI_API_KEY
 *  - OPENAI_BASE_URL
 *  - OPENAI_MODEL
 *  - MYLINEHUB_BASE_URL
 *  - MYLINEHUB_LOGIN_URL
 *  - MYLINEHUB_LOGIN_USERNAME
 *  - MYLINEHUB_LOGIN_PASSWORD
 *  - RAG_VECTOR_STORE_URL
 *  - EMAIL_REPORT_URL
 */
@Entity
@Table(name = "system_config", indexes = {
		  @Index(name = "system_config_key", columnList = "config_key")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true, length = 128)
    private String key;

    @Column(name = "config_value", nullable = false, length = 4096)
    private String value;
}
