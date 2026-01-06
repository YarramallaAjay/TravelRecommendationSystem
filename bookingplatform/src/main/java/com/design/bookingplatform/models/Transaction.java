package com.design.bookingplatform.models;

import com.design.bookingplatform.models.Journey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_status", columnList = "status"),
        @Index(name = "idx_transaction_created", columnList = "createdAt"),
        @Index(name = "idx_booking_id", columnList = "bookingId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transactionId; // External payment gateway transaction ID

    @Column(nullable = false)
    private String bookingId; // Links to journey booking

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id", nullable = false)
    private Journey journey;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(length = 100)
    private String paymentGateway; // Razorpay, Paytm, etc.

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime expiresAt; // Payment expiry (3-4 mins from creation)

    // Metadata for reconciliation
    @Column(columnDefinition = "TEXT")
    private String gatewayResponse;

    @Version
    private Long version; // Optimistic locking
}

enum TransactionStatus {
    INITIATED,
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    CANCELLED,
    REFUNDED,
    REFUND_PENDING,
    EXPIRED
}

enum PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    UPI,
    NET_BANKING,
    WALLET,


    EMI
}