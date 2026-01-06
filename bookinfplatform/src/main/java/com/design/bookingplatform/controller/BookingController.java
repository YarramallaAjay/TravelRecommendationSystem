package com.design.bookingplatform.controller;

import com.design.bookingplatform.models.Journey;
import com.design.bookingplatform.models.Train;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookingplatform")
public class ExternalWebController {

    @GetMapping("trains/availability/{trainNumber}")
    public ResponseEntity<Train> fetchTrainAvailability(@PathVariable int trainNumber){
        return ResponseEntity.ok(new Train());
    }

    @GetMapping("/trains/")
    public ResponseEntity<List<Train>> fetchAllTrains(){
        return ResponseEntity.ok(List.of(new Train()));
    }

    @GetMapping("/trains/routes/{station1}/station2")
    public ResponseEntity<?> fetchroutesByStations(@PathVariable String startingStation, @PathVariable String destStation){
        return ResponseEntity.ok(new Train());
    }


    @PostMapping("/trains/booking")
    public ResponseEntity<Train> makeNewBooking(@RequestBody Journey journeyDetails){
        return ResponseEntity.ok(new Train());
    }

    @PutMapping("/trains/lock/{trainNumber}/{coachAndSeatNumber}")
    public ResponseEntity<Train> acquireSeatLock(@PathVariable int trainNumber, @PathVariable String coachAndSeatNumber){
        return ResponseEntity.ok(new Train());
    }

    @PutMapping("/trains/lock/{trainNumber}/{coachAndSeatNumber}")
    public ResponseEntity<Train> releaseSeatLock(@PathVariable int trainNumber, @PathVariable String coachAndSeatNumber){
        return ResponseEntity.ok(new Train());
    }


}
