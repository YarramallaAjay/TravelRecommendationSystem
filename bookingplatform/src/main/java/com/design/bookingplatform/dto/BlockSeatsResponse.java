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
public class BlockSeatsResponse {
    private String bookingReference;
    private String status; // "BLOCKED", "PARTIALLY_BLOCKED", "FAILED"
    private List<BlockedTicketDTO> tickets;
    private BigDecimal totalFare;
    private Instant expiresAt;
    private String message;
}
