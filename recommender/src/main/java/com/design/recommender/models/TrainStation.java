package com.design.recommender.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "train_stations", indexes = {
        @Index(name = "idx_train_station", columnList = "train_id,stationOrder")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(nullable = false, length = 100)
    private String stationCode;

    @Column(nullable = false, length = 100)
    private String stationName;

    @Column(nullable = false)
    private Integer stationOrder; // Sequence in route (1, 2, 3...)

    @Column(nullable = false)
    private LocalTime arrivalTime;

    @Column(nullable = false)
    private LocalTime departureTime;

    @Column
    private Integer haltTimeMinutes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceFromSource; // in KM

    @Column
    private String platform;
}