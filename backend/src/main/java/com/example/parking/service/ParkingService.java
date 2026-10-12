package com.example.parking.service;

import com.example.parking.kafka.KafkaProducer;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class ParkingService {

    private final KafkaProducer producer;

    public ParkingService(KafkaProducer producer){
        this.producer = producer;
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
}
