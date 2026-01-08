package com.design.bookingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachDTO {
    private Long id;
    private String coachNumber;
    private String coachClass;
    private Integer totalSeats;
    private Integer availableSeats;
    private BigDecimal baseFare;
    private Boolean hasAC;
    private Boolean hasCharging;
    private Boolean hasWifi;
}
