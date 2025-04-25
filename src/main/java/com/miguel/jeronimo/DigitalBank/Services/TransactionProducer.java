package com.miguel.jeronimo.DigitalBank.Services;

import com.miguel.jeronimo.DigitalBank.Entities.Transaction;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionProducer {

    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    public TransactionProducer(KafkaTemplate<String, Transaction> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, Transaction transaction) {
        kafkaTemplate.send(topic, transaction);
    }
}
