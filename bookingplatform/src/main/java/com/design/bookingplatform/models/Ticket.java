package com.design.bookingplatform.models;

import com.design.bookingplatform.models.Coach;
import com.design.bookingplatform.models.Journey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_pnr", columnList = "pnrNumber"),
        @Index(name = "idx_ticket_journey", columnList = "journey_id"),
        @Index(name = "idx_ticket_train_date", columnList = "train_id,journeyDate")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String pnrNumber; // Unique PNR from booking platform

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id", nullable = false)
    private Journey journey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @Column(nullable = false, length = 10)
    private String seatNumber;

    @Column(nullable = false, length = 100)
    private String passengerName;

    @Column(nullable = false)
    private Integer passengerAge;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender passengerGender;

    @Column(nullable = false, length = 100)
    private String boardingStation;

    @Column(nullable = false, length = 100)
    private String destinationStation;

    @Column(nullable = false)
    private LocalDate journeyDate;

    @Column(nullable = false)
    private LocalTime departureTime;

    @Column(nullable = false)
    private LocalTime arrivalTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime bookedAt;

    @Column
    private LocalDateTime confirmedAt;

    @Column
    private LocalDateTime cancelledAt;

    // For seat blocking mechanism
    @Column
    private LocalDateTime blockedAt;

    @Column
    private LocalDateTime blockExpiresAt;

    @Column(length = 100)
    private String externalBookingReference; // Reference from external platform

    @Version
    private Long version; // Optimistic locking
}

enum Gender {
    MALE,
    FEMALE,
    OTHER
}

enum TicketStatus {
    DRAFT,
    CHECKING_AVAILABILITY,
    AVAILABLE,
    BLOCKED,          // Temporarily blocked for payment
    BLOCK_EXPIRED,    // Block expired due to timeout
    CONFIRMED,        // Booking confirmed
    WAIT_LISTED,      // On waiting list
    CANCELLED,        // User cancelled
    REFUNDED          // Refund processed
}