package com.example.vote_service.exception;

import lombok.Getter;

@Getter
public enum VoteExceptionCode {
	AGENDA_NOT_FOUND(404, "Agenda not found"),
	SELECT_OPTION_NOT_FOUND(404, "Select option not found"),
	ONGOING_SECRET_VOTE(400, "This agenda is ongoing secret vote"),
	NO_RIGHT_FOR(404, "This user has no right for the action"),
	VOTE_HISTORY_NOT_FOUND(404, "Vote history not found"),
	AFTER_VOTE_END_DATE(404, "Vote end date is over"),
	NO_VOTE(200, "No vote" ),
	SECRET_VOTE(404, "The information is secret" );

	VoteExceptionCode(int statusCode, String message) {
		this.statusCode = statusCode;
		this.message = message;
	}

	private final int statusCode;
	private final String message;
}
