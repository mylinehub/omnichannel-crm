package com.mylinehub.aiemail.model;

/**
 * Connection type for an email account.
 *
 * IMAP_IDLE  - this service will establish an IMAP connection (IDLE with polling fallback).
 * SIEVE_HTTP - messages are pushed into this service via HTTP (e.g. Dovecot + Sieve + pipe).
 */
public enum EmailConnectionType {
    IMAP_IDLE,
    SIEVE_HTTP
}
