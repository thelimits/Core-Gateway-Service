package com.s8.demoservice.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.s8.demoservice.service.MessageHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.s8.demoservice.constant.Topics.TRANSACTION_TOPIC;

@Component
@Slf4j
public class TMconsumer {
    @Autowired
    private MessageHandlerService handler;
    @KafkaListener(topics = TRANSACTION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void receiveTransaction(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        log.info("Received payload: '{}'", consumerRecord.value());
        handler.handleMessage(consumerRecord.topic(), consumerRecord.value());
    }
}
