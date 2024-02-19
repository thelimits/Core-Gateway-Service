package com.s8.demoservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.s8.demoservice.EventKafka.TransactionEvent;
import com.s8.demoservice.dto.PostingInstructionDTO;
import com.s8.demoservice.dto.TransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.s8.demoservice.constant.Topics.TRANSACTION_TOPIC;

@Service
@Slf4j
public class MessageHandlerService {
    private static final Logger LOG = LoggerFactory.getLogger(MessageHandlerService.class);
    @Value("${proxyUrl}")
    private String restProxyUrl;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void handleMessage(final String topic, final String message) throws JsonProcessingException {
        switch (topic) {
            case TRANSACTION_TOPIC:
                ingestTransaction(message);
                break;
            default:
                break;
        }
    }

    private void ingestTransaction(String message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);
        TransactionDTO postingInstruction = event.getPosting_instruction_batches().get(0);
        event.setValueTimestamp(postingInstruction.getValue_timestamp());
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/vnd.kafka.json.v2+json");
        headers.set(HttpHeaders.ACCEPT, "application/vnd.kafka.v2+json");
        HttpEntity<TransactionDTO> entity = new HttpEntity<>(postingInstruction, headers);
        restTemplate.postForEntity(restProxyUrl + "/topics/" + TRANSACTION_TOPIC, entity, String.class);
    }
}
