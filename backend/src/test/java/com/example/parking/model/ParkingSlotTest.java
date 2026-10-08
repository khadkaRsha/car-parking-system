package com.example.parking.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.parking.model.ParkingSlot.Status;


public class ParkingSlotTest {

	ParkingSlot parkingSlotModel;
	Car carModel;


    @BeforeEach 
    public void setUp()
    {
    
    	parkingSlotModel = new ParkingSlot();
        carModel = new Car();
        carModel.setCarNumber("JS-RK888");
        
        parkingSlotModel.setId(50L);
        parkingSlotModel.setSlotNumber(5);
        parkingSlotModel.setCar(carModel);
        parkingSlotModel.setStatus(Status.OCCUPIED);
    	
    }
    
	@Test
	@DisplayName("Should correctly update and return the parking slot status")
	public void getStatusTest() {
		ParkingSlot.Status expectedStatus = ParkingSlot.Status.FREE;
		//when 
		parkingSlotModel.setStatus(expectedStatus);
		//Then
	    assertEquals(expectedStatus, parkingSlotModel.getStatus());

		
	}
	
	@Test
	@DisplayName("should return null values")
	public void getStatusTestNull() {
		ParkingSlot.Status expectedStatus = null;
		//when 
		parkingSlotModel.setStatus(expectedStatus);
		//Then
	    assertEquals(expectedStatus, parkingSlotModel.getStatus());

		
	}
	
	@Test
	@DisplayName("should return slotNumber")
	public void getSlotnumberTest() {
		Integer expectedSlotNumber = 2;
		//when 
		parkingSlotModel.setSlotNumber(expectedSlotNumber);
		//Then
	    assertEquals(expectedSlotNumber, parkingSlotModel.getSlotNumber());
	
	}
	
	@Test
	@DisplayName("should return car objects-car number")
	public void getCarTest() {

		assertEquals(carModel, parkingSlotModel.getCar());
	
	}
	
	@Test
    @DisplayName("Should correctly return assigned values")
    void testGettersAndSetters() {
        assertAll("ParkingSlot State Verification",
            () -> assertEquals(50L, parkingSlotModel.getId()),
            () -> assertEquals(5, parkingSlotModel.getSlotNumber()),
            () -> assertEquals(Status.OCCUPIED, parkingSlotModel.getStatus()),
            () -> assertEquals(carModel, parkingSlotModel.getCar())
        );
    }
	
	@Test
	@DisplayName("Should throw IllegalArgumentException when converting invalid string to Status enum")
	void testInvalidEnumValueThrowsException() {
	    assertThrows(IllegalArgumentException.class, () -> {
	        Status.valueOf("NOSTATUS");
	    });
	}
	
	
	@Test
	@DisplayName("Should verify that OCCUPIED is not equal to FREE")
	void checkENUMValuesTest() {
	    // Given
	    parkingSlotModel.setStatus(ParkingSlot.Status.OCCUPIED);

	    // Then
	    assertNotEquals(ParkingSlot.Status.FREE, parkingSlotModel.getStatus());
	}
	
	@Test
	@DisplayName("Should contain expected enum values")
	void testEnumValues() {
	    ParkingSlot.Status[] statuses = ParkingSlot.Status.values();

	    assertEquals(3, statuses.length);
	    assertEquals(ParkingSlot.Status.FREE, ParkingSlot.Status.valueOf("FREE"));
	    assertEquals(ParkingSlot.Status.IN_PROCESS, ParkingSlot.Status.valueOf("IN_PROCESS"));
	    assertEquals(ParkingSlot.Status.OCCUPIED, ParkingSlot.Status.valueOf("OCCUPIED"));
	}
	
	@Test
	@DisplayName("Should allow removing a car from the slot")
	void testRemoveCarFromSlot() {
		// car is removed
        parkingSlotModel.setCar(null);
        parkingSlotModel.setStatus(Status.FREE);
		
		//then
		assertEquals(ParkingSlot.Status.FREE, parkingSlotModel.getStatus());
		assertNull(parkingSlotModel.getCar());	}
	
	@AfterEach 
    public void tearDown()
    {
    	parkingSlotModel = null;
    	carModel = null;
    }


}
