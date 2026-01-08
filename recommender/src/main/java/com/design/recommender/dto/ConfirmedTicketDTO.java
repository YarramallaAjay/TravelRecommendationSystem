package com.design.bookingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmedTicketDTO {
    private String pnrNumber;
    private String trainNumber;
    private String coachNumber;
    private String seatNumber;
    private String passengerName;
    private String boardingStation;
    private String destinationStation;
    private LocalDate journeyDate;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private BigDecimal fare;
    private String status; // "CONFIRMED"
}
