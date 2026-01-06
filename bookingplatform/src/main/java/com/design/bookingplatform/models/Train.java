package com.design.bookingplatform.models;

import com.design.bookingplatform.models.Coach;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trains", indexes = {
        @Index(name = "idx_train_number", columnList = "trainNumber"),
        @Index(name = "idx_train_route", columnList = "sourceStation,destinationStation")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String trainNumber;

    @Column(nullable = false, length = 100)
    private String trainName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainType trainType;

    @Column(nullable = false, length = 100)
    private String sourceStation;

    @Column(nullable = false, length = 100)
    private String destinationStation;

    @Column(nullable = false)
    private LocalTime departureTime;

    @Column(nullable = false)
    private LocalTime arrivalTime;

    @Column(nullable = false)
    private Integer journeyDurationMinutes;

    @Column(length = 50)
    private String operatingDays; // MON,TUE,WED,THU,FRI,SAT,SUN

    @Column
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Coach> coaches = new ArrayList<>();

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TrainStation> stations = new ArrayList<>(); // Intermediate stops
}

enum TrainType {
    EXPRESS,
    SUPERFAST,
    MAIL,
    PASSENGER,
    RAJDHANI,
    SHATABDI,
    DURONTO,
    VANDE_BHARAT
}