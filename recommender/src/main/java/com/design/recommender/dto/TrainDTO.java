package com.design.bookingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainDTO {
    private Long id;
    private String trainNumber;
    private String trainName;
    private String trainType;
    private String sourceStation;
    private String destinationStation;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Integer journeyDurationMinutes;
    private String operatingDays;
    private Boolean isActive;
}
