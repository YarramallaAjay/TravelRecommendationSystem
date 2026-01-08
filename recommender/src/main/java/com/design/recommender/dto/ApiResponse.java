package com.design.bookingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

// ============================================================================
// COMMON RESPONSE WRAPPER
// ============================================================================

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private Boolean success;
    private String message;
    private T data;
    private Integer count;
    private Instant timestamp;
    private String errorCode;
    private List<String> errors;
}

// ============================================================================
// TRAIN DTOs
// ============================================================================






// ============================================================================
// AVAILABILITY DTOs
// ============================================================================


// ============================================================================
// SEAT LOCKING DTOs
// ============================================================================

// ============================================================================
// BOOKING DTOs
// ============================================================================

// ============================================================================
// CANCELLATION & REFUND DTOs
// ============================================================================

