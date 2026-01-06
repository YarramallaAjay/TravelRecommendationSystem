package com.design.bookingplatform.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatLockRequest {
    @NotBlank(message = "Train number is required")
    private String trainNumber;

    @NotNull(message = "Journey date is required")
    private LocalDate journeyDate;

    @NotEmpty(message = "At least one seat is required")
    private List<SeatIdentifier> seats;

    @NotBlank(message = "Booking reference is required")
    private String bookingReference;

    @Min(value = 1, message = "Lock duration must be at least 1 minute")
    @Max(value = 10, message = "Lock duration cannot exceed 10 minutes")
    private Integer lockDurationMinutes = 3;
}
