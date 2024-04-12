package com.example.vote_service.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public abstract class BaseEntity {

	private LocalDateTime createdAt;

	protected BaseEntity(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
