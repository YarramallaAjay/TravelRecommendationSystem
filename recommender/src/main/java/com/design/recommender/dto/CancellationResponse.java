package com.design.bookingplatform.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancellationResponse {

    @NotBlank(message = "Reason is required")
    int bookingId;

    @NotBlank(message = "Reason is required")
    int cancellationId;

    String booking_reason;
}
