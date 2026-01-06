package com.design.bookingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {
    private String trainNumber;
    private LocalDate date;
    private String status; // "AVAILABLE", "WAITLIST", "FULL", "NOT_OPERATING"
    private List<CoachAvailabilityDTO> coaches;
    private Integer totalAvailableSeats;
    private Integer waitlistCount;
}