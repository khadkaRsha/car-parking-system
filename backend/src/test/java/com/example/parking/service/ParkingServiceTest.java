package com.example.parking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.parking.kafka.KafkaProducer;
import com.example.parking.model.Car;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {
	
	@InjectMocks
	ParkingService parkingServiceMock;
	
	@Mock
	KafkaProducer kafkaProduecerMock;
	
	@BeforeEach
	void setup() {
		Car car = new Car();
		car.setCarNumber("JS-RK8888");
	}
	
	@AfterEach
	void tearDown() {
		Car car = null;
	}
	
	@Test
	void testSetup() {
		assertTrue(true, "Successfully configured Test environment");
	}
	
	@Test
	@DisplayName("Should send car number")
	void testcarEnteringToParkingSlot() {
		String carNumber = "NE-RK-1001";
		
		parkingServiceMock.enterParking(carNumber);
        
        verify(kafkaProduecerMock, times(1)).sendCar(carNumber);
        
       
	}
	
	
	  @Test
	  @DisplayName("Should return 0 fee when exit is within the 15-minute grace period")
	  void testCalculateParkingFeeWithinGracePeriod() {
	      LocalDateTime entryTime = LocalDateTime.of(2026, 10, 12, 10, 0);
	      LocalDateTime exitTime = LocalDateTime.of(2026, 10, 12, 10, 12);
	      
	      BigDecimal expectedResult = BigDecimal.ZERO;

	      BigDecimal actualResult = parkingServiceMock.calculateParkingFee(entryTime, exitTime);

	      assertEquals(expectedResult, actualResult);
	  }

	  @Test
	  @DisplayName("Should calculate €3.00 for parking 45 minutes (rounds up to 1 hour)")
	  void testCalculateParkingFeeExceedGracePeriod() {
	      LocalDateTime entryTime = LocalDateTime.of(2026, 10, 12, 10, 0);
	      LocalDateTime exitTime = LocalDateTime.of(2026, 10, 12, 10, 45);
	      BigDecimal expectedResult = new BigDecimal("3.00");

	      BigDecimal actualResult = parkingServiceMock.calculateParkingFee(entryTime, exitTime);

	      assertEquals(expectedResult, actualResult);
	  }
	  
	  @Test
	  @DisplayName("Should cap the fee at €25.00 for long stays")
	  void testCalculateParkingFeeDailyCap() {
	      // Entry at 08:00, Exit at 20:00 (12 hours)
	      LocalDateTime entryTime = LocalDateTime.of(2026, 10, 12, 8, 0);
	      LocalDateTime exitTime = LocalDateTime.of(2026, 10, 12, 20, 0);
	      BigDecimal expectedResult = new BigDecimal("25.00");

	      BigDecimal actualResult = parkingServiceMock.calculateParkingFee(entryTime, exitTime);

	      assertEquals(expectedResult, actualResult);
	  }
	
}
