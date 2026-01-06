package com.design.bookingplatform.controller;

import com.design.bookingplatform.dto.*;
import com.design.bookingplatform.service.BookingService;
import com.design.bookingplatform.service.TrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * External Booking Platform API Controller
 * This simulates the external train booking platform (like IRCTC)
 * Your recommender service will call these endpoints
 */
@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private final TrainService trainService;

    @Autowired
    private final BookingService bookingService;


    /**
     * Get all active trains
     * GET /api/platform/trains
     */
    @GetMapping("/trains")
    public ResponseEntity<ApiResponse<List<TrainDTO>>> getAllTrains(
            @RequestParam(required = false) Boolean isActive
    ) {
        log.info("Fetching all trains, isActive: {}", isActive);
        List<TrainDTO> trains = trainService.getAllTrains(isActive);

        return ResponseEntity.ok(ApiResponse.<List<TrainDTO>>builder()
                .success(true)
                .message("Trains fetched successfully")
                .data(trains)
                .timestamp(java.time.Instant.now())
                .build());
    }

    /**
     * Get train by train number
     * GET /api/platform/trains/{trainNumber}
     */
    @GetMapping("/trains/{trainNumber}")
    public ResponseEntity<ApiResponse<TrainDTO>> getTrainByNumber(
            @PathVariable String trainNumber
    ) {
        log.info("Fetching train details for trainNumber: {}", trainNumber);
        TrainDTO train = trainService.getTrainByNumber(trainNumber);

        return ResponseEntity.ok(ApiResponse.<TrainDTO>builder()
                .success(true)
                .message("Train details fetched successfully")
                .data(train)
                .timestamp(java.time.Instant.now())
                .build());
    }

    /**
     * Get trains by route (source to destination)
     * GET /api/platform/trains/routes
     * Query params: source, destination
     */
    @GetMapping("/trains/routes")
    public ResponseEntity<ApiResponse<List<TrainDTO>>> getTrainsByRoute(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("Fetching trains from {} to {} on date: {}", source, destination, date);
        List<TrainDTO> trains = trainService.getTrainsByRoute(source, destination, date);

        return ResponseEntity.ok(ApiResponse.<List<TrainDTO>>builder()
                .success(true)
                .message("Trains fetched successfully")
                .data(trains)
                .count(trains.size())
                .timestamp(java.time.Instant.now())
                .build());
    }

    /**
     * Get train schedule with all intermediate stations
     * GET /api/platform/trains/{trainNumber}/schedule
     */
    @GetMapping("/trains/{trainNumber}/schedule")
    public ResponseEntity<ApiResponse<TrainScheduleDTO>> getTrainSchedule(
            @PathVariable String trainNumber
    ) {
        log.info("Fetching schedule for train: {}", trainNumber);
        TrainScheduleDTO schedule = trainService.getTrainSchedule(trainNumber);

        return ResponseEntity.ok(ApiResponse.<TrainScheduleDTO>builder()
                .success(true)
                .message("Train schedule fetched successfully")
                .data(schedule)
                .timestamp(java.time.Instant.now())
                .build());
    }

    /**
     * Check seat availability for a specific train
     * GET /api/platform/trains/{trainNumber}/availability
     * Query params: date, coachClass, source, destination
     */
    @GetMapping("/trains/{trainNumber}/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkAvailability(
            @PathVariable String trainNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String coachClass,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String destination
    ) {
        log.info("Checking availability for train: {}, date: {}, class: {}",
                trainNumber, date, coachClass);

        AvailabilityResponse availability = trainService.checkAvailability(
                trainNumber, date, coachClass, source, destination
        );

        return ResponseEntity.ok(ApiResponse.<AvailabilityResponse>builder()
                .success(true)
                .message("Availability checked successfully")
                .data(availability)
                .timestamp(java.time.Instant.now())
                .build());
    }

    /**
     * Bulk availability check for multiple trains
     * POST /api/platform/trains/bulk-availability
     * Body: { "trainIds": ["12345", "12346"], "date": "2025-01-15", "coachClass": "3AC" }
     */
    @PostMapping("/trains/bulk-availability")
    public ResponseEntity<ApiResponse<Map<String, AvailabilityResponse>>> checkBulkAvailability(
            @Valid @RequestBody BulkAvailabilityRequest request
    ) {
        log.info("Bulk availability check for {} trains on {}",
                request.getTrainNumbers().size(), request.getDate());

        Map<String, AvailabilityResponse> availabilityMap =
                trainService.checkBulkAvailability(request);

        return ResponseEntity.ok(ApiResponse.<Map<String, AvailabilityResponse>>builder()
                .success(true)
                .message("Bulk availability checked successfully")
                .data(availabilityMap)
                .count(availabilityMap.size())
                .timestamp(java.time.Instant.now())
                .build());
    }

    /**
     * Get available coaches and seats for a train
     * GET /api/platform/trains/{trainNumber}/coaches
     */
    @GetMapping("/trains/{trainNumber}/coaches")
    public ResponseEntity<ApiResponse<List<CoachDTO>>> getAvailableCoaches(
            @PathVariable String trainNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("Fetching available coaches for train: {} on {}", trainNumber, date);
        List<CoachDTO> coaches = trainService.getAvailableCoaches(trainNumber, date);

        return ResponseEntity.ok(ApiResponse.<List<CoachDTO>>builder()
                .success(true)
                .message("Coaches fetched successfully")
                .data(coaches)
                .timestamp(java.time.Instant.now())
                .build());
    }



    /**
     * Acquire seat lock (block seats temporarily)
     * PUT /api/platform/seats/lock
     * Body: { "trainNumber": "12345", "seats": [...], "lockDurationMinutes": 3 }
     */
    @PutMapping("/seats/lock")
    public ResponseEntity<ApiResponse<SeatLockResponse>> acquireSeatLock(
            @Valid @RequestBody SeatLockRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey
    ) {
        log.info("Acquiring seat lock for train: {}, seats: {}, idempotencyKey: {}",
                request.getTrainNumber(), request.getSeats(), idempotencyKey);

        SeatLockResponse lockResponse = bookingService.acquireSeatLock(request, idempotencyKey);

        return ResponseEntity.ok(ApiResponse.<SeatLockResponse>builder()
                .success(true)
                .message("Seats locked successfully")
                .data(lockResponse)
                .timestamp(java.time.Instant.now())
                .build());
    }

    /**
     * Release seat lock
     * DELETE /api/platform/seats/lock/{lockId}
     */
    @DeleteMapping("/seats/lock/{lockId}")
    public ResponseEntity<ApiResponse<Void>> releaseSeatLock(
            @PathVariable String lockId
    ) {
        log.info("Releasing seat lock: {}", lockId);
        bookingService.releaseSeatLock(lockId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Seat lock released successfully")
                .timestamp(java.time.Instant.now())
                .build());
    }


    /**
     * Block seats (temporary reservation before payment)
     * POST /api/platform/seats/block
     * Body: { "trainNumber": "12345", "passengers": [...], "journey": {...} }
     */
    @PostMapping("/seats/block")
    public ResponseEntity<ApiResponse<BlockSeatsResponse>> blockSeats(
            @Valid @RequestBody BlockSeatsRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey
    ) {
        log.info("Blocking seats for train: {}, passengers: {}, idempotencyKey: {}",
                request.getTrainNumber(), request.getPassengers().size(), idempotencyKey);

        BlockSeatsResponse blockResponse = bookingService.blockSeats(request, idempotencyKey);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<BlockSeatsResponse>builder()
                        .success(true)
                        .message("Seats blocked successfully")
                        .data(blockResponse)
                        .timestamp(java.time.Instant.now())
                        .build());
    }

    /**
     * Confirm booking (after successful payment)
     * POST /api/platform/bookings/confirm
     * Body: { "bookingReference": "BLK-20250115-ABC123", "paymentTransactionId": "PAY123" }
     */
    @PostMapping("/bookings/confirm")
    public ResponseEntity<ApiResponse<BookingConfirmationResponse>> confirmBooking(
            @Valid @RequestBody ConfirmBookingRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey
    ) {
        log.info("Confirming booking: {}, payment: {}, idempotencyKey: {}",
                request.getBookingReference(), request.getPaymentTransactionId(), idempotencyKey);

        BookingConfirmationResponse confirmation =
                bookingService.confirmBooking(request, idempotencyKey);

        return ResponseEntity.ok(ApiResponse.<BookingConfirmationResponse>builder()
                .success(true)
                .message("Booking confirmed successfully")
                .data(confirmation)
                .timestamp(java.time.Instant.now())
                .build());
    }

    /**
     * Release blocked seats (if payment fails/times out)
     * POST /api/platform/seats/release
     * Body: { "bookingReference": "BLK-20250115-ABC123" }
     */
    @PostMapping("/seats/release")
    public ResponseEntity<ApiResponse<Void>> releaseSeats(
            @Valid @RequestBody ReleaseSeatRequest request
    ) {
        log.info("Releasing seats for booking: {}", request.getBookingReference());
        bookingService.releaseSeats(request.getBookingReference());

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Seats released successfully")
                .timestamp(java.time.Instant.now())
                .build());
    }

    /**
     * Get booking details
     * GET /api/platform/bookings/{bookingId}
     */
    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<ApiResponse<BookingDetailsResponse>> getBookingDetails(
            @PathVariable String bookingId
    ) {
        log.info("Fetching booking details: {}", bookingId);
        BookingDetailsResponse booking = bookingService.getBookingDetails(bookingId);

        return ResponseEntity.ok(ApiResponse.<BookingDetailsResponse>builder()
                .success(true)
                .message("Booking details fetched successfully")
                .data(booking)
                .timestamp(java.time.Instant.now())
                .build());
    }

    /**
     * Cancel booking
     * POST /api/platform/bookings/{bookingId}/cancel
     */
    @PostMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<CancellationResponse>> cancelBooking(
            @PathVariable String bookingId,
            @Valid @RequestBody CancellationRequest request
    ) {
        log.info("Cancelling booking: {}, reason: {}", bookingId, request.getReason());
        CancellationResponse cancellation = bookingService.cancelBooking(bookingId, request);

        return ResponseEntity.ok(ApiResponse.<CancellationResponse>builder()
                .success(true)
                .message("Booking cancelled successfully")
                .data(cancellation)
                .timestamp(java.time.Instant.now())
                .build());
    }

    // ========================================================================
    // FARE CALCULATION ENDPOINTS
    // ========================================================================

//    /**
//     * Calculate fare for a journey
//     * POST /api/platform/fare/calculate
//     * Body: { "trainNumber": "12345", "source": "NDLS", "destination": "CSTM", ... }
//     */
//    @PostMapping("/fare/calculate")
//    public ResponseEntity<ApiResponse<FareResponse>> calculateFare(
//            @Valid @RequestBody FareCalculationRequest request
//    ) {
//        log.info("Calculating fare for train: {} from {} to {}",
//                request.getTrainNumber(), request.getSource(), request.getDestination());
//
//        FareResponse fare = trainService.calculateFare(request);
//
//        return ResponseEntity.ok(ApiResponse.<FareResponse>builder()
//                .success(true)
//                .message("Fare calculated successfully")
//                .data(fare)
//                .timestamp(java.time.Instant.now())
//                .build());
//    }
//

//    /**
//     * Get PNR status
//     * GET /api/platform/pnr/{pnrNumber}
//     */
//    @GetMapping("/pnr/{pnrNumber}")
//    public ResponseEntity<ApiResponse<PNRStatusResponse>> getPNRStatus(
//            @PathVariable String pnrNumber
//    ) {
//        log.info("Fetching PNR status: {}", pnrNumber);
//        PNRStatusResponse pnrStatus = bookingService.getPNRStatus(pnrNumber);
//
//        return ResponseEntity.ok(ApiResponse.<PNRStatusResponse>builder()
//                .success(true)
//                .message("PNR status fetched successfully")
//                .data(pnrStatus)
//                .timestamp(java.time.Instant.now())
//                .build());
//    }


//    /**
//     * Get all stations
//     * GET /api/platform/stations
//     */
//    @GetMapping("/stations")
//    public ResponseEntity<ApiResponse<List<StationDTO>>> getAllStations() {
//        log.info("Fetching all stations");
//        List<StationDTO> stations = trainService.getAllStations();
//
//        return ResponseEntity.ok(ApiResponse.<List<StationDTO>>builder()
//                .success(true)
//                .message("Stations fetched successfully")
//                .data(stations)
//                .timestamp(java.time.Instant.now())
//                .build());
//    }
//
//    /**
//     * Search stations by name or code
//     * GET /api/platform/stations/search?query=delhi
//     */
//    @GetMapping("/stations/search")
//    public ResponseEntity<ApiResponse<List<StationDTO>>> searchStations(
//            @RequestParam String query
//    ) {
//        log.info("Searching stations with query: {}", query);
//        List<StationDTO> stations = trainService.searchStations(query);
//
//        return ResponseEntity.ok(ApiResponse.<List<StationDTO>>builder()
//                .success(true)
//                .message("Stations search completed")
//                .data(stations)
//                .timestamp(java.time.Instant.now())
//                .build());
//    }
//
//
//
//
//    /**
//     * Health check endpoint
//     * GET /api/platform/health
//     */
//    @GetMapping("/health")
//    public ResponseEntity<ApiResponse<HealthCheckResponse>> healthCheck() {
//        HealthCheckResponse health = HealthCheckResponse.builder()
//                .status("UP")
//                .timestamp(java.time.Instant.now())
//                .version("1.0.0")
//                .build();
//
//        return ResponseEntity.ok(ApiResponse.<HealthCheckResponse>builder()
//                .success(true)
//                .message("Service is healthy")
//                .data(health)
//                .timestamp(java.time.Instant.now())
//                .build());
//    }
//
//    /**
//     * Webhook for booking status updates
//     * POST /api/platform/webhooks/booking-status
//     * (External platform calls this when booking status changes)
//     */
//    @PostMapping("/webhooks/booking-status")
//    public ResponseEntity<ApiResponse<Void>> bookingStatusWebhook(
//            @RequestBody BookingStatusWebhookPayload payload,
//            @RequestHeader("X-Webhook-Signature") String signature
//    ) {
//        log.info("Received booking status webhook: bookingId={}, status={}",
//                payload.getBookingId(), payload.getStatus());
//
//        // Verify signature and process webhook
//        bookingService.processBookingStatusWebhook(payload, signature);
//
//        return ResponseEntity.ok(ApiResponse.<Void>builder()
//                .success(true)
//                .message("Webhook processed successfully")
//                .timestamp(java.time.Instant.now())
//                .build());
//    }
}