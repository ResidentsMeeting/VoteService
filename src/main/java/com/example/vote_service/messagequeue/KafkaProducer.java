package com.example.vote_service.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {
	private final ObjectMapper mapper;
	private final KafkaSender<String, Object> producer;

	public Mono<MessageProduceResult> send(Event message) {

		Mono<ProducerRecord<String, Object>> recordMono = null;
		try {
			recordMono = Mono.just(new ProducerRecord<>(message.getTopicName(), null, mapper.writeValueAsString(message.getRequestedMessage())));
		} catch (JsonProcessingException e) {
			recordMono = Mono.error(e);
		}

		return producer.createOutbound()
				.send(recordMono)
				.then()
				.thenReturn(new MessageProduceResult(message))
				.onErrorResume(e -> Mono.just(new MessageProduceResult(message, e)));
	}
}
