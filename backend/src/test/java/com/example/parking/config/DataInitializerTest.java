package com.example.parking.config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.parking.model.ParkingSlot;
import com.example.parking.repository.ParkingSlotRepository;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;

@ExtendWith(MockitoExtension.class)
public class DataInitializerTest {
	
	@Test
	void testSetup() {
		assertTrue(true, "Successfully configured Test environment");
	}
	
	@Mock
    private ParkingSlotRepository repository; 

    @InjectMocks
    private DataInitializer dataInitializer; // Injects the mock repository into DataInitializer

    @Test
    @DisplayName("Should create 50 slots and flush when database is empty")
    void run_WhenDatabaseIsEmpty_ShouldInitialize50Slots() throws Exception {
        // 1. Arrange: Tell the mock repo to return 0 when count() is called
        when(repository.count()).thenReturn(0L);

        // 2. Act: Execute the CommandLineRunner logic directly
        dataInitializer.run();

        // 3. Assert: Verify save() was called exactly 50 times and flush() was called once
        verify(repository, times(50)).save(any(ParkingSlot.class));
        verify(repository, times(1)).flush();
    }

    @Test
    @DisplayName("Should skip slot creation when slots already exist in database")
    void run_WhenDatabaseIsNotEmpty_ShouldSkipInitialization() throws Exception {
        // 1. Arrange: Tell the mock repo to return 50 when count() is called
        when(repository.count()).thenReturn(50L);

        // 2. Act: Execute the runner
        dataInitializer.run();

        // 3. Assert: Verify save() and flush() were NEVER called
        verify(repository, never()).save(any(ParkingSlot.class));
        verify(repository, never()).flush();
    }
	

}


