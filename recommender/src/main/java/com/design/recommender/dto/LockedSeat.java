package com.design.bookingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockedSeat {
    private String coachNumber;
    private String seatNumber;
    private String status; // "LOCKED", "UNAVAILABLE"
}
