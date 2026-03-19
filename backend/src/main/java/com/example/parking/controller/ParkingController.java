package com.example.parking.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.parking.dto.CarRequest;
import com.example.parking.kafka.KafkaProducer;
import com.example.parking.model.ParkingSlot;
import com.example.parking.repository.ParkingSlotRepository;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/parking")
public class ParkingController {

    @Autowired
    private KafkaProducer producer;
    @Autowired
    private ParkingSlotRepository slotRepo;

    @PostMapping("/enter")
    public String enterCar(@RequestBody CarRequest request) {
        producer.sendCar(request.getCarNumber());
        return "Car sent to Kafka: " + request.getCarNumber();
    }

    @GetMapping("/slots")
    public List<ParkingSlot> getSlots() {
        return slotRepo.findAll();
    }
}