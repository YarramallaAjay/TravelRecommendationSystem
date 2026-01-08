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
public class BlockedTicketDTO {
    private String pnrNumber;
    private String coachNumber;
    private String seatNumber;
    private String passengerName;
    private BigDecimal fare;
    private String status; // "BLOCKED", "UNAVAILABLE"
}
