package com.example.parking.service;

import com.example.parking.kafka.KafkaProducer;
import org.springframework.stereotype.Service;

@Service
public class ParkingService {

    private final KafkaProducer producer;

    public ParkingService(KafkaProducer producer){
        this.producer = producer;
    }

    public void enterParking(String carNumber){
        producer.sendCar(carNumber);
    }
}
