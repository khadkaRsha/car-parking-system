package com.example.parking.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.example.parking.model.Car;
import com.example.parking.model.ParkingSlot;
import com.example.parking.model.ParkingSlot.Status;
import com.example.parking.repository.CarRepository;
import com.example.parking.repository.ParkingSlotRepository;

@ExtendWith(MockitoExtension.class)
public class KafkaConsumerTest {

	@InjectMocks
	KafkaConsumer kafkaConsumerMock;

	@Mock
	KafkaTemplate<String, String> kafkaTemplateMock;
	@Mock
    ParkingSlotRepository parkingSlotRepository;
	@Mock
    CarRepository carRepository;


	@Test
	public void testConsumeCarCreate() {
		Car carMock = new Car();
		carMock.setCarNumber("RK-JES1122");
		
		String carNumber = "RK-JES1122";
		
		ParkingSlot parkingSlotModel = new ParkingSlot();
        parkingSlotModel.setId(50L);
        parkingSlotModel.setSlotNumber(5);
        parkingSlotModel.setCar(carMock);
        parkingSlotModel.setStatus(Status.FREE);
        
        when(carRepository.findByCarNumber(carNumber)).thenReturn(Optional.of(carMock));
        when(parkingSlotRepository.existsByCar(carMock)).thenReturn(false);
        when(parkingSlotRepository.findFirstByStatus(ParkingSlot.Status.FREE))
                .thenReturn(Optional.of(parkingSlotModel));

        kafkaConsumerMock.consumeCar(carNumber);

        verify(carRepository, times(1)).findByCarNumber(carNumber);
        verify(parkingSlotRepository, times(1)).existsByCar(carMock);
        
        verify(parkingSlotRepository, times(1)).findFirstByStatus(ParkingSlot.Status.FREE);
        verify(parkingSlotRepository, times(1)).save(parkingSlotModel);

        assertEquals(ParkingSlot.Status.OCCUPIED, parkingSlotModel.getStatus());
        assertEquals(carMock, parkingSlotModel.getCar());	
	}
	
	@Test
	public void consumeCar_GarageFull_SlotNotSaved() {
	    String carNumber = "RK-JES1122";
	    Car car = new Car();
	    car.setCarNumber(carNumber);

	    when(carRepository.findByCarNumber(carNumber)).thenReturn(Optional.of(car));
	    when(parkingSlotRepository.existsByCar(car)).thenReturn(false);

	    when(parkingSlotRepository.findFirstByStatus(ParkingSlot.Status.FREE)).thenReturn(Optional.empty());
	    kafkaConsumerMock.consumeCar(carNumber);

	    verify(carRepository, times(1)).findByCarNumber(carNumber);
	    verify(parkingSlotRepository, times(1)).existsByCar(car);
	    verify(parkingSlotRepository, times(1)).findFirstByStatus(ParkingSlot.Status.FREE);
	    verify(parkingSlotRepository, never()).save(any(ParkingSlot.class));
	}
}
