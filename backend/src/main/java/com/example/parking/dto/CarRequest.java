package com.example.parking.dto;

import java.util.List;

public class CarRequest {
	
	private String carNumber;
    private List<String> carNumbers; // Enables bulk/batch entry

    public CarRequest() {
    }

    public CarRequest(String carNumber) {
        this.carNumber = carNumber;
    }

    public CarRequest(List<String> carNumbers) {
        this.carNumbers = carNumbers;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public List<String> getCarNumbers() {
        return carNumbers;
    }

    public void setCarNumbers(List<String> carNumbers) {
        this.carNumbers = carNumbers;
    }
}
