package com.mylinehub.aiemail.service;

/**
 * Helper to fetch strongly-typed configuration values from SystemConfig table.
 */
public interface SystemConfigService {

    String getRequired(String key);

    String getOptional(String key, String defaultValue);
}
