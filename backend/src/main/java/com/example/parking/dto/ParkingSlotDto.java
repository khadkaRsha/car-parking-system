package com.example.parking.dto;

public class ParkingSlotDto {

	private Long id;
	private Integer slotNumber;
	private String status;
	private String carNumber;

	public ParkingSlotDto() {
	}

	public ParkingSlotDto(Long id, Integer slotNumber, String status, String carNumber) {
		this.id = id;
		this.slotNumber = slotNumber;
		this.status = status;
		this.carNumber = carNumber;
	}

	public Long getId() {
	    return id != null ? id : null;
	}
	

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getSlotNumber() {
		return slotNumber;
	}

	public void setSlotNumber(Integer slotNumber) {
		this.slotNumber = slotNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCarNumber() {
		return carNumber;
	}

	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}
}