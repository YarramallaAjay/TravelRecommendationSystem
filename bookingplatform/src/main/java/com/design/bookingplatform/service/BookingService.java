package com.design.bookingplatform.service;

import com.design.bookingplatform.dto.*;
import org.springframework.stereotype.Service;

@Service
public class BookingService {
    public SeatLockResponse acquireSeatLock(SeatLockRequest request, String idempotencyKey) {
        return null;
    }

    public void releaseSeatLock(String lockId) {
    }

    public BlockSeatsResponse blockSeats(BlockSeatsRequest request, String idempotencyKey) {
        return null;
    }

    public BookingConfirmationResponse confirmBooking(ConfirmBookingRequest request, String idempotencyKey) {
        return null;
    }

    public void releaseSeats(String bookingReference) {
    }

    public BookingDetailsResponse getBookingDetails(String bookingId) {
        return null;
    }

    public CancellationResponse cancelBooking(String bookingId, CancellationRequest request) {
        return null;
    }
}
