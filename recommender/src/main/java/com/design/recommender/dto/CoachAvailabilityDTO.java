package com.design.bookingplatform.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachAvailabilityDTO {
    private Long coachId;
    private String coachNumber;
    private String coachClass;
    private Integer totalSeats;
    private Integer availableSeats;
    private BigDecimal fare;
    private Boolean hasAC;
    private Boolean hasCharging;
    private Boolean hasWifi;
    private String status; // "AVAILABLE", "RAC", "WAITLIST"
}