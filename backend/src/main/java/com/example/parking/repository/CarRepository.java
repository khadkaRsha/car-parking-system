package com.example.parking.repository;

import com.example.parking.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car,Long> {

    Optional<Car> findByCarNumber(String carNumber);
}
