package com.example.vote_service.messagequeue;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
	private final Environment env;
	// 기본 설정들로 구성
	@Bean("kafkaSender")
	public KafkaSender<String, Object> kafkaSender() {
		SenderOptions<String, Object> senderOptions = SenderOptions.create(getProducerProps());
		senderOptions.scheduler(Schedulers.parallel());
		senderOptions.closeTimeout(Duration.ofSeconds(5));
		return KafkaSender.create(senderOptions);
	}

	// 프로듀서 옵션
	private Map<String, Object> getProducerProps() {

		Map<String,Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, env.getProperty("spring.kafka.bootstrap-servers"));
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 1000);
		return props;
	}
}