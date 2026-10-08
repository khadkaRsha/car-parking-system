package com.example.parking.kafka;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
public class KafkaProducerTest {

@InjectMocks
KafkaProducer kafkaProducerMock;

@Mock
KafkaTemplate<String, String> kafkaTemplateMock;

@Test
public void testSendCar() {
	kafkaProducerMock.sendCar("RK-JS4444");
	verify(kafkaTemplateMock, times(1)).send("parking-topic", "RK-JS4444");
}

}
