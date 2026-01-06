package com.design.bookingplatform.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "coaches", indexes = {
        @Index(name = "idx_train_coach", columnList = "train_id,coachNumber")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(nullable = false, length = 10)
    private String coachNumber; // A1, B2, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoachClass coachClass;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer availableSeats;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Column
    private Boolean hasAC = false;

    @Column
    private Boolean hasCharging = false;

    @Column
    private Boolean hasWifi = false;
}

enum CoachClass {
    FIRST_AC("1A"),
    SECOND_AC("2A"),
    THIRD_AC("3A"),
    SLEEPER("SL"),
    CHAIR_CAR("CC"),
    EXECUTIVE_CHAIR("EC"),
    GENERAL("GN");

    private final String code;

    CoachClass(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}