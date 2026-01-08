package com.design.bookingplatform.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmBookingRequest {
    @NotBlank(message = "Booking reference is required")
    private String bookingReference;

    @NotBlank(message = "Payment transaction ID is required")
    private String paymentTransactionId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Payment amount must be positive")
    private BigDecimal paymentAmount;
}
