package com.example.vote_service.domain.dto;

import com.example.vote_service.messagequeue.Event;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class VoteEvent extends Event {

	private VoteEvent(VotePayload votePayload) {
		super("vote_sink", votePayload);
	}

	public static VoteEvent toEvent(VoteCreationDto voteCreationDto, Long userId) {
		return new VoteEvent(new VotePayload(voteCreationDto.selectOptionId(), userId, LocalDateTime.now()));
	}

	private record VotePayload(
			Long selectOptionId,
			Long userId,
			LocalDateTime createdAt
	) {
	}
}
