package com.example.parking.controller;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.parking.dto.CarRequest;
import com.example.parking.dto.ParkingSlotDto;
import com.example.parking.kafka.KafkaProducer;
import com.example.parking.model.ParkingSession;
import com.example.parking.repository.ParkingSessionRepository;
import com.example.parking.repository.ParkingSlotRepository;
import com.example.parking.service.ParkingService;


@RestController
@CrossOrigin("*")
@RequestMapping("/api/parking")
@Transactional(readOnly = true)
public class ParkingController {

    @Autowired
    private KafkaProducer producer;
    @Autowired
    private ParkingSlotRepository slotRepo;
    @Autowired
    private ParkingSessionRepository parkingSessionRepository;
    @Autowired
    private ParkingService parkingService; // 
    
    @PostMapping("/enter")
    public String enterCar(@RequestBody CarRequest request) {
        producer.sendCar(request.getCarNumber());
        return "Car sent to Kafka: " + request.getCarNumber();
    }

    
 // 2. Bulk Car Entry (Reuses CarRequest DTO)
    @PostMapping("/enter-batch")
    public String enterCarsBatch(@RequestBody CarRequest request) {
        if (request.getCarNumbers() != null && !request.getCarNumbers().isEmpty()) {
            for (String carNumber : request.getCarNumbers()) {
                producer.sendCar(carNumber);
            }
            return request.getCarNumbers().size() + " cars sent to Kafka queue.";
        }
        return "No car numbers provided.";
    }

    // 3. Get All Slots with Flattened License Plate Data
    @GetMapping("/slots")
    public List<ParkingSlotDto> getSlots() {
        return slotRepo.findAll().stream()
                .map(slot -> {
                    Long id = slot.getId(); // Slot ID (1 to 8)
                    Integer slotNum = slot.getSlotNumber(); // Slot number (1 to 8)
               
                    String statusStr = slot.getStatus() != null ? String.valueOf(slot.getStatus()) : "FREE";
                    
                    // Check if car exists before extracting license plate
                    String carNum = slot.getCar() != null ? slot.getCar().getCarNumber() : null;

                    return new ParkingSlotDto(id, slotNum, statusStr, carNum);
                })
                .collect(Collectors.toList());
    }
    
    @GetMapping("/details/{car_number}")
    public List<ParkingSession> getCarDetails(@PathVariable("car_number") String carNumber) {
        return parkingSessionRepository.findByCarCarNumber(carNumber);
    }
    

    @PostMapping("/exit/{slotId}")
    public ResponseEntity<String> exitCar(@PathVariable("slotId") Long slotId) {
        try {
            parkingService.exitCar(slotId);
            return ResponseEntity.ok("Car successfully removed from slot.");
        } catch (Exception e) {
            e.printStackTrace(); // PRINT EXACT JAVA STACKTRACE TO CONSOLE
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Exit failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/search/{carNumber}")
    public ResponseEntity<List<ParkingSession>> searchCar(@PathVariable("carNumber") String carNumber) {
        List<ParkingSession> sessions = parkingSessionRepository.findByCarCarNumberContainingIgnoreCase(carNumber.trim());
        if (sessions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sessions);
    }
    
}