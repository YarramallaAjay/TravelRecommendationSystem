package com.design.recommender.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys", indexes = {
        @Index(name = "idx_idempotency_key", columnList = "idempotencyKey", unique = true),
        @Index(name = "idx_expires_at", columnList = "expiresAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String idempotencyKey; // UUID from client

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requestHash; // SHA-256 of request body

    @Column(columnDefinition = "TEXT")
    private String responseBody; // Cached response

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdempotencyStatus status;

    @Column(nullable = false, length = 50)
    private String operationType; // BLOCK_SEATS, CONFIRM_BOOKING, etc.

    @Column(length = 100)
    private String entityId; // Related bookingId or journeyId

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt; // TTL for cleanup (24 hours)

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Version
    private Long version; // Optimistic locking to prevent duplicates
}

enum IdempotencyStatus {
    PROCESSING,
    COMPLETED,
    FAILED
}