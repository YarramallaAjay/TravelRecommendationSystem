package com.design.bookingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatIdentifier {
    private String coachNumber;
    private String seatNumber;
}
