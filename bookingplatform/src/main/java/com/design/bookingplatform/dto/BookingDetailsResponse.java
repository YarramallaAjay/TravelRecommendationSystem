package com.design.bookingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailsResponse {
    private String bookingId;
    private String trainNumber;
    private String trainName;
    private LocalDate journeyDate;
    private String sourceStation;
    private String destinationStation;
    private String status;
    private List<ConfirmedTicketDTO> tickets;
    private BigDecimal totalFare;
    private Instant bookedAt;
    private Instant confirmedAt;
}
