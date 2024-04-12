package com.example.vote_service.messagequeue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

@Getter
public abstract class Event {
	@JsonIgnore
	private final String topicName;
	private final Object requestedMessage;

	protected Event(String topicName, Object payload) {
		this.topicName = topicName;
		this.requestedMessage = payload;
	}
}
