package com.design.bookingplatform.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journeys", indexes = {
        @Index(name = "idx_journey_status", columnList = "status"),
        @Index(name = "idx_journey_date", columnList = "journeyDate"),
        @Index(name = "idx_booking_id", columnList = "bookingId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Journey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bookingId; // User-facing booking reference

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String sourceStation;

    @Column(nullable = false, length = 100)
    private String destinationStation;

    @Column(nullable = false)
    private LocalDate journeyDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JourneyStatus status;

    @Enumerated(EnumType.STRING)
    private JourneyType journeyType; // SINGLE_TRAIN, MULTI_TRAIN

    @Column(precision = 10, scale = 2)
    private BigDecimal totalFare;

    @Column
    private Integer totalTravelTimeMinutes; // Total journey duration

    @Column
    private Integer totalLayoverMinutes; // Total waiting time between trains

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime confirmedAt;

    @Column
    private LocalDateTime cancelledAt;

    // Relationships
    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();

    @Version
    private Long version; // Optimistic locking
}

enum JourneyStatus {
    DRAFT,              // User building the journey
    AVAILABILITY_CHECK, // Checking seat availability
    SEATS_BLOCKED,      // Seats temporarily blocked
    PAYMENT_PENDING,    // Waiting for payment
    PAYMENT_FAILED,     // Payment failed
    CONFIRMED,          // Booking confirmed
    CANCELLED,          // User cancelled
    COMPLETED           // Journey completed
}

enum JourneyType {
    SINGLE_TRAIN,
    MULTI_TRAIN
}