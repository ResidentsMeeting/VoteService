package com.example.vote_service.domain;

import lombok.Getter;


@Getter
public class Vote extends BaseEntity {

	private Long id;

	private Long selectOptionId;

	private Long userId;

	public Vote(Long selectOptionId, Long userId) {
		this.selectOptionId = selectOptionId;
		this.userId = userId;
	}

	public Vote() {
	}
}
