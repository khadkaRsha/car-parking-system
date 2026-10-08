package com.example.parking.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.parking.dto.CarRequest;
import com.example.parking.dto.ParkingSlotDto;
import com.example.parking.kafka.KafkaProducer;
import com.example.parking.model.Car;
import com.example.parking.model.ParkingSlot;
import com.example.parking.model.ParkingSlot.Status;
import com.example.parking.repository.ParkingSlotRepository;

@ExtendWith(MockitoExtension.class)
public class ParkingControllerTest {

	@InjectMocks
	ParkingController parkingController;

	@Mock
	KafkaProducer kafkaProduecerMock;
	
	@Mock
	ParkingSlotRepository parkingSlotRepository;
	
	CarRequest carRequest;
	
	ParkingSlotDto parkingSlotDto;
	ParkingSlot parkingSlot;
	Car carModel;
	
//	ParkingSlotDto(id, slotNum, statusStr, carNum)

	@BeforeEach
	public void setup() {
		carRequest = new CarRequest();
		carRequest.setCarNumber("JS-RK8888");
		
		carModel = new Car();
	    carModel.setCarNumber("JS-RK888");
		
		// assign parking slot DTO
	    parkingSlotDto = new ParkingSlotDto();
		parkingSlotDto.setCarNumber("JS-RK8888");
		parkingSlotDto.setId(1L);
		parkingSlotDto.setSlotNumber(1);
		parkingSlotDto.setStatus("OCCUPIED");
		
		
		parkingSlot = new ParkingSlot();
    
		parkingSlot.setId(50L);
		parkingSlot.setSlotNumber(5);
		parkingSlot.setCar(carModel);
		parkingSlot.setStatus(Status.OCCUPIED);
	}

	
	@Test
	@DisplayName("Display all the slots available in the parking lot")
    public void testGetSlots() {
	    Car car = new Car();
	    car.setCarNumber("JS-RK8888");

	    ParkingSlot occupiedSlot = new ParkingSlot();
	    occupiedSlot.setId(1L);
	    occupiedSlot.setSlotNumber(1);
	    occupiedSlot.setStatus(ParkingSlot.Status.OCCUPIED);
	    occupiedSlot.setCar(car);

	    ParkingSlot freeSlot = new ParkingSlot();
	    freeSlot.setId(2L);
	    freeSlot.setSlotNumber(2);
	    freeSlot.setStatus(ParkingSlot.Status.FREE);
	    freeSlot.setCar(null);
	    
		when(parkingSlotRepository.findAll()).thenReturn(List.of(occupiedSlot, freeSlot));

         List<ParkingSlotDto> expectedResult = parkingController.getSlots();
         
         assertNotNull(expectedResult, "Resulting DTO list should not be null");
         assertEquals(2, expectedResult.size(), "Should return exactly 2 slots");
         
         ParkingSlotDto dto1 = expectedResult.get(0);
         assertEquals(1L, dto1.getId());
         assertEquals(1, dto1.getSlotNumber());
         assertEquals("OCCUPIED", dto1.getStatus());
         assertEquals("JS-RK8888", dto1.getCarNumber());

         ParkingSlotDto dto2 = expectedResult.get(1);
         assertEquals(2L, dto2.getId());
         assertEquals(2, dto2.getSlotNumber());
         assertEquals("FREE", dto2.getStatus());
         assertNull(dto2.getCarNumber(), "Car number should be null for empty slot");

         verify(parkingSlotRepository, times(1)).findAll();
	}
	
	@Test
	@DisplayName("Should enter the car in batch")
	void testEnterBatch() {
		CarRequest carRequest =  new CarRequest();
		
		List<String> carNumbers = new ArrayList<>();
		carNumbers.add("JS-RK0000");
		carNumbers.add("JS-RK9999");
		carNumbers.add("JS-RK8888");
		
		carRequest.setCarNumbers(carNumbers);
		String actualResult =parkingController.enterCarsBatch(carRequest);
		
		verify(kafkaProduecerMock, times(3)).sendCar(any());
		
		assertEquals("3 cars sent to Kafka queue.", actualResult);

	}
	
	/*@Test
	@DisplayName("Should enter the car in batch")
	void testEnterBatchSuccess() {
		CarRequest carRequest= new CarRequest();
		carRequest = null;
		parkingController.enterCarsBatch(carRequest);
		assertEquals("No car numbers provided", "No car numbers provided");
	}
	
	
	@Test
	@DisplayName("Should enter the car in batch")
	void testEnterBatchFailed() {
		CarRequest carRequest= new CarRequest();
		carRequest = null;
		parkingController.enterCarsBatch(carRequest);
		assertEquals("No car numbers provided", "No car numbers provided");
	}*/

	@Test
	@DisplayName("Should send carRequest object to kafka producer")
	void testCarEnter() {
		parkingController.enterCar(carRequest);
		verify(kafkaProduecerMock, times(1)).sendCar("JS-RK8888");

	}
	
	


}
