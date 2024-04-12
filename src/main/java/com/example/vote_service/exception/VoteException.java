package com.example.vote_service.exception;

import lombok.Getter;

@Getter
public class VoteException extends RuntimeException {

	private final int statusCode;

	public VoteException(VoteExceptionCode voteExceptionCode) {
		super(voteExceptionCode.getMessage());
		this.statusCode = voteExceptionCode.getStatusCode();
	}
}
