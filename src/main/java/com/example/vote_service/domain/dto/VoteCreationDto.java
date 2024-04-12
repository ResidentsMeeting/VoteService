package com.example.vote_service.domain.dto;

import jakarta.validation.constraints.Min;
public record VoteCreationDto(
		@Min(0) Long selectOptionId) {
}
