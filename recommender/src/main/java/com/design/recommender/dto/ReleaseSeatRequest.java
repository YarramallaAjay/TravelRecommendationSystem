package com.design.bookingplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseSeatRequest {
    @NotBlank(message = "Booking reference is required")
    private String bookingReference;
}
