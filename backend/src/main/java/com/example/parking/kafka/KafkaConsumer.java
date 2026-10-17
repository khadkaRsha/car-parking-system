package com.example.parking.kafka;

import com.example.parking.model.Car;
import com.example.parking.model.ParkingSession;
import com.example.parking.model.ParkingSlot;
import com.example.parking.repository.CarRepository;
import com.example.parking.repository.ParkingSessionRepository;
import com.example.parking.repository.ParkingSlotRepository;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @Autowired
    private ParkingSlotRepository slotRepo;
    
    @Autowired
    private ParkingSessionRepository parkingSessionRepository;

    @Autowired
    private CarRepository carRepo;

    @Transactional // CRITICAL: Ensures the DB update is committed
    @KafkaListener(topics = "parking-topic", groupId = "parking-group")
    public void consumeCar(String carNumber) {
        // 1. Get or Create the car
        Car car = carRepo.findByCarNumber(carNumber)
                .orElseGet(() -> {
                    Car newCar = new Car();
                    newCar.setCarNumber(carNumber);
                    return carRepo.save(newCar);
                });

        // 2. Prevent the same car from taking multiple slots
        if (slotRepo.existsByCar(car)) {
            System.out.println("Wait! Car " + carNumber + " is already parked.");
            return;
        }

     // 3. Find a FREE slot and update it
        slotRepo.findFirstByStatus(ParkingSlot.Status.FREE)
            .ifPresentOrElse(slot -> {
                slot.setCar(car);
                slot.setStatus(ParkingSlot.Status.OCCUPIED);
                slotRepo.save(slot); // Save the slot update

                // 4. CREATE & SAVE THE PARKING SESSION
                ParkingSession session = new ParkingSession();
                session.setCar(car);
                session.setSlot(slot);
                session.setEntryTime(LocalDateTime.now());
                session.setFee(BigDecimal.ZERO);
                parkingSessionRepository.save(session);

                System.out.println("SUCCESS: Slot " + slot.getSlotNumber() + " assigned to " + carNumber + " and parking session recorded.");
            }, () -> {
                System.out.println("FAILED: No free slots available for " + carNumber);
            });
    }
}