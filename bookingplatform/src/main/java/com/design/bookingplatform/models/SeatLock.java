package com.design.bookingplatform.models;

import com.design.bookingplatform.models.Train;
import com.design.bookingplatform.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat_locks", indexes = {
        @Index(name = "idx_lock_key", columnList = "lockKey", unique = true),
        @Index(name = "idx_expires_at", columnList = "expiresAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String lockKey; // Format: "lock:seat:{trainId}:{seatId}:{date}"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(nullable = false, length = 10)
    private String seatNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String bookingId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime lockedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt; // Auto-release after 3-4 minutes

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LockStatus status;
}

enum LockStatus {
    ACTIVE,
    RELEASED,
    EXPIRED
}