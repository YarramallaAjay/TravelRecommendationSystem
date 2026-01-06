package com.design.bookingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationDTO {
    private String stationCode;
    private String stationName;
    private String city;
    private String state;
}