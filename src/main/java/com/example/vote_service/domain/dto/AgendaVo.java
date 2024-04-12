package com.example.vote_service.domain.dto;

import com.example.vote_service.domain.AgendaHistory;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AgendaVo(Long id,
					   String apartmentCode,
					   String title,
					   String details,
					   LocalDate endDate,
					   boolean secret,
					   LocalDateTime createdAt,
					   List<SelectOptionVo> selectOptionList) {
	public AgendaHistory toAgendaHistory() {

		return AgendaHistory.builder()
				.id(id)
				.apartmentCode(apartmentCode)
				.title(title)
				.details(details)
				.endDate(endDate)
				.selectOptions(selectOptionList.stream().map(SelectOptionVo::toSelectOptionHistory).toList())
				.build();
	}
}
