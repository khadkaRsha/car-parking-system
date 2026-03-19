package com.example.parking.config;

import com.example.parking.model.ParkingSlot;
import com.example.parking.repository.ParkingSlotRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;

@Component // Use @Component to ensure Spring finds it automatically
public class DataInitializer implements CommandLineRunner {

    private final ParkingSlotRepository repository;

    public DataInitializer(ParkingSlotRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional // Ensures all 50 slots are saved in one go
    public void run(String... args) throws Exception {
        long count = repository.count();
        System.out.println("DEBUG: Current slot count in DB is: " + count);

        if (count == 0) {
            System.out.println("--- STARTING INITIALIZATION: Creating 50 Slots ---");
            for (int i = 1; i <= 50; i++) {
                ParkingSlot slot = new ParkingSlot();
                slot.setSlotNumber(i);
                slot.setStatus(ParkingSlot.Status.FREE);
                repository.save(slot);
            }
            // Force a flush to the database
            repository.flush(); 
            System.out.println("--- SUCCESS: 50 Slots are now in the Database ---");
        } else {
            System.out.println("--- SKIP: Slots already exist in Database ---");
        }
    }
}