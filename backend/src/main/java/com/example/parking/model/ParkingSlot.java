package com.example.parking.model;

import jakarta.persistence.*;
import com.example.parking.model.Car;
@Entity
@Table(name = "parking_slots")
public class ParkingSlot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

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