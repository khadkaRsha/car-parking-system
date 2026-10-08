package com.example.parking.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
	
}
