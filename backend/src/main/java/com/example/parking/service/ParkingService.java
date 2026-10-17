package com.example.parking.service;

import com.example.parking.kafka.KafkaProducer;
import com.example.parking.model.ParkingSession;
import com.example.parking.model.ParkingSlot;
import com.example.parking.repository.ParkingSessionRepository;
import com.example.parking.repository.ParkingSlotRepository;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class ParkingService {

	private final KafkaProducer producer;
    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingSlotRepository parkingSlotRepository;

    // Proper constructor injection for all dependencies
    public ParkingService(
            KafkaProducer producer,
            ParkingSessionRepository parkingSessionRepository,
            ParkingSlotRepository parkingSlotRepository) {
        this.producer = producer;
        this.parkingSessionRepository = parkingSessionRepository;
        this.parkingSlotRepository = parkingSlotRepository;
    }    


    public void enterParking(String carNumber){
        producer.sendCar(carNumber);
    }
    
    public BigDecimal calculateParkingFee(LocalDateTime entryTime, LocalDateTime exitTime) {
        long minutesParked = Duration.between(entryTime, exitTime).toMinutes();

        // 1. Check Grace Period
        if (minutesParked <= 15) {
            return BigDecimal.ZERO;
        }

        // 2. Calculate Hours (rounding up)
        long hoursParked = (long) Math.ceil((double) minutesParked / 60.0);

        // 3. Multiply by hourly rate (e.g., €3.00 per hour)
        BigDecimal hourlyRate = new BigDecimal("3.00");
        BigDecimal totalFee = hourlyRate.multiply(BigDecimal.valueOf(hoursParked));

        // 4. Apply Daily Cap (e.g., max €25.00)
        BigDecimal dailyCap = new BigDecimal("25.00");
        if (totalFee.compareTo(dailyCap) > 0) {
            return dailyCap;
        }

        return totalFee;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void exitCar(Long slotId) {
        // 1. Fetch the slot safely
        ParkingSlot slot = parkingSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found with ID: " + slotId));

        if (slot.getStatus() != ParkingSlot.Status.OCCUPIED) {
            // If slot is already free in DB, just clean it up on UI side without throwing 500
            slot.setStatus(ParkingSlot.Status.FREE);
            slot.setCar(null);
            parkingSlotRepository.saveAndFlush(slot);
            return;
        }

        // 2. Find active session (using stream or findFirst to prevent multiple result crashes if data is dirty)
        Optional<ParkingSession> sessionOpt = parkingSessionRepository.findBySlotIdAndExitTimeIsNull(slotId);
        
        LocalDateTime now = LocalDateTime.now();

        if (sessionOpt.isPresent()) {
            ParkingSession session = sessionOpt.get();
            LocalDateTime entryTime = session.getEntryTime() != null ? session.getEntryTime() : now;            
            session.setExitTime(now);
            // Safe fee calculation
            try {
                session.setFee(calculateParkingFee(entryTime, now));
            } catch (Exception e) {
                session.setFee(BigDecimal.ZERO); // Fallback fee if calculation fails
            }
            parkingSessionRepository.saveAndFlush(session);
        }

        // 3. Free the slot and disassociate car (Ensure no cascade deletes are on the @OneToOne mapping)
        slot.setStatus(ParkingSlot.Status.FREE);
        slot.setCar(null); 
        parkingSlotRepository.saveAndFlush(slot);
    }
    
    public List<ParkingSession> searchHistory(String carNumber) {
        if (carNumber == null || carNumber.trim().isEmpty()) {
            return parkingSessionRepository.findAll();
        }
        return parkingSessionRepository.findByCarCarNumberContainingIgnoreCase(carNumber.trim());
    }
}
