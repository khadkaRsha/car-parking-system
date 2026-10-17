package com.example.parking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.parking.dto.ParkingSlotDto;
import com.example.parking.kafka.KafkaProducer;
import com.example.parking.model.Car;
import com.example.parking.model.ParkingSession;
import com.example.parking.model.ParkingSlot;
import com.example.parking.model.ParkingSlot.Status;
import com.example.parking.repository.ParkingSessionRepository;
import com.example.parking.repository.ParkingSlotRepository;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	@InjectMocks
	ParkingService parkingServiceMock;

	@Mock
	KafkaProducer kafkaProduecerMock;

	@Mock
	ParkingSessionRepository parkingSessionRepository;
	
	@Mock
	ParkingSlotRepository parkingSlotRepository;

	ParkingSlotDto parkingSlotDto;
	ParkingSlot parkingSlot;
	Car carModel;
	ParkingSession parkingSession;

	@BeforeEach
	void setup() {
		carModel = new Car();
		carModel.setCarNumber("JS-RK8888");

		parkingSlot = new ParkingSlot();
		parkingSlot.setId(50L);
		parkingSlot.setSlotNumber(5);
		parkingSlot.setCar(carModel);
		parkingSlot.setStatus(Status.OCCUPIED);

	    parkingSession = new ParkingSession();
		parkingSession.setId(1L);
		parkingSession.setCar(carModel);
		parkingSession.setSlot(parkingSlot);

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

	@Test
	@DisplayName("testSearchVehicle")
	void testSearchCarWhenCar() {

		when(parkingSessionRepository.findByCarCarNumberContainingIgnoreCase(carModel.getCarNumber().trim()))
				.thenReturn(List.of(parkingSession));
		List<ParkingSession> result = parkingServiceMock.searchHistory(carModel.getCarNumber());

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("JS-RK8888", result.get(0).getCar().getCarNumber());
		assertEquals(5, result.get(0).getSlot().getSlotNumber());
		assertEquals(Status.OCCUPIED, result.get(0).getSlot().getStatus());
	}
	
	@Test
	@DisplayName("testSearchVehicle")
	void testSearchCarWhenNoCar() {
		String carNumber = "JS-RK8888";

		when(parkingSessionRepository.findByCarCarNumberContainingIgnoreCase(carModel.getCarNumber().trim()))
				.thenReturn(null);
		List<ParkingSession> result = parkingServiceMock.searchHistory(carModel.getCarNumber());
		assertNull(null);
	
	}
	
	@Test
    void testExitCar_SlotNotFound() {
        when(parkingSlotRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        	parkingServiceMock.exitCar(99L);
        });

        assertEquals("Slot not found with ID: 99", exception.getMessage());
        verify(parkingSlotRepository, never()).saveAndFlush(any()); 
    }
	@Test
    void testExitCar_SlotNotOccupied() {
        parkingSlot.setStatus(ParkingSlot.Status.FREE);
        when(parkingSlotRepository.findById(1L)).thenReturn(Optional.of(parkingSlot));

        parkingServiceMock.exitCar(1L);

        assertEquals(ParkingSlot.Status.FREE, parkingSlot.getStatus());
        assertNull(parkingSlot.getCar());
        verify(parkingSlotRepository).saveAndFlush(parkingSlot);
        verify(parkingSessionRepository, never()).findBySlotIdAndExitTimeIsNull(any());
        verify(parkingSessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void testExitCar_OccupiedWithActiveSession() {
        when(parkingSlotRepository.findById(1L)).thenReturn(Optional.of(parkingSlot));
        when(parkingSessionRepository.findBySlotIdAndExitTimeIsNull(1L)).thenReturn(Optional.of(parkingSession));
        parkingServiceMock.exitCar(1L);
        
        assertNotNull(parkingSession.getExitTime());
        assertNotNull(parkingSession.getFee());
        assertEquals(ParkingSlot.Status.FREE, parkingSlot.getStatus());
        assertNull(parkingSlot.getCar());

        verify(parkingSessionRepository).saveAndFlush(parkingSession);
        verify(parkingSlotRepository).saveAndFlush(parkingSlot);
    }

    @Test
    void testExitCar_OccupiedWithoutActiveSession() {
        when(parkingSlotRepository.findById(1L)).thenReturn(Optional.of(parkingSlot));
        when(parkingSessionRepository.findBySlotIdAndExitTimeIsNull(1L)).thenReturn(Optional.empty());

        parkingServiceMock.exitCar(1L);

        assertEquals(ParkingSlot.Status.FREE, parkingSlot.getStatus());
        assertNull(parkingSlot.getCar());

        verify(parkingSessionRepository, never()).saveAndFlush(any());
        verify(parkingSlotRepository).saveAndFlush(parkingSlot);
    }

}
