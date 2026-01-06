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
public class BlockSeatsRequest {
    @NotBlank(message = "Train number is required")
    private String trainNumber;

    @NotNull(message = "Journey date is required")
    private LocalDate journeyDate;

    @NotBlank(message = "Source station is required")
    private String sourceStation;

    @NotBlank(message = "Destination station is required")
    private String destinationStation;

    @NotBlank(message = "Coach class is required")
    private String coachClass;

    @NotEmpty(message = "At least one passenger is required")
    @Size(max = 6, message = "Maximum 6 passengers allowed")
    private List<PassengerDTO> passengers;

    @Min(value = 1, message = "Block duration must be at least 1 minute")
    @Max(value = 10, message = "Block duration cannot exceed 10 minutes")
    private Integer blockDurationMinutes = 3;
}
