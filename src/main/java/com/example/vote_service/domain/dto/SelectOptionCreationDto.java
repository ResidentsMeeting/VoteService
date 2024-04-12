package com.example.vote_service.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record SelectOptionCreationDto(
		@NotBlank
		String summary,
		String details
) {
}
