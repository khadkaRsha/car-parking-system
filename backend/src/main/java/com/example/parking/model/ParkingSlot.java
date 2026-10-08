package com.example.parking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "parking_slots")
public class ParkingSlot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private int slotNumber;

	@Enumerated(EnumType.STRING) // This saves "FREE" or "OCCUPIED" as text in DB
	private Status status;

	@OneToOne // Changed to OneToOne because one slot holds one car
    @JoinColumn(name="car_id")
    private Car car;

	// --- The Enum Definition ---
	public enum Status {
		FREE, IN_PROCESS, OCCUPIED
	}


	public ParkingSlot() {
		// TODO Auto-generated constructor stub
	}

	// Getters and Setters...
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	public int getSlotNumber() {
		return slotNumber;
	}

	public void setSlotNumber(int slotNumber) {
		this.slotNumber = slotNumber;
	}
}