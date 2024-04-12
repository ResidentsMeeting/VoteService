package com.example.vote_service.messagequeue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class MessageProduceResult {
	private Boolean status = true;
	private final String topic;
	@JsonIgnore
	private final Object requestedMessage;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String errorMessage = null;
	public MessageProduceResult(Event event) {
		this.topic = event.getTopicName();
		this.requestedMessage = event.getRequestedMessage();
	}

	public MessageProduceResult(Event event, Throwable e) {
		this(event);
		this.status = false;
		this.errorMessage = e.getMessage();
	}
}
