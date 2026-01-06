package com.design.bookingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingConfirmationResponse {
    private String bookingId;
    private String status; // "CONFIRMED", "FAILED"
    private List<ConfirmedTicketDTO> tickets;
    private BigDecimal totalAmount;
    private Instant confirmedAt;
    private String message;
}
