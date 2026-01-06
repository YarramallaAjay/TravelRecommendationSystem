package com.design.bookingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLockResponse {
    private String lockId;
    private String status; // "LOCKED", "FAILED"
    private List<LockedSeat> lockedSeats;
    private Instant expiresAt;
    private String message;
}
