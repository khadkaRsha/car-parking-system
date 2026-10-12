package com.example.parking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.parking.model.ParkingSession;

@Repository
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {
	// Spring automatically joins Car and matches the carNumber string!
    List<ParkingSession> findCarDetails(String carNumber);
	
    // Find if a car currently has an active open parking session
    Optional<ParkingSession> findByCarIdAndExitTimeIsNull(Long carId);
}