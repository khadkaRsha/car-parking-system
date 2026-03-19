package com.example.parking.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.parking.model.Car;
import com.example.parking.model.ParkingSlot;

public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {
    // Finds the first FREE slot via SQL
    Optional<ParkingSlot> findFirstByStatus(ParkingSlot.Status status);
    
    // Checks if this car is already assigned to a slot
    boolean existsByCar(Car car);
}