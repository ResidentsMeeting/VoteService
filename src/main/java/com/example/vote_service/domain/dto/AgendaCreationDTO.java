package com.example.vote_service.domain.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record AgendaCreationDTO(
		@NotBlank
		String title,
		String details,
		@Future
		@NotNull
		LocalDate endDate,
		@NotNull
		Boolean secret,
		@NotEmpty
		List<SelectOptionCreationDto> selectOptionCreationDtoList
) {
}
