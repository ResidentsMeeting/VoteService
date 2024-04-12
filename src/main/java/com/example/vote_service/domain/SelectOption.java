package com.example.vote_service.domain;

import com.example.vote_service.domain.dto.SelectOptionCreationDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SelectOption extends BaseEntity {
	private Long id;

	private Long agendaId;

	private String summary;

	private String details;

	@Builder
	protected SelectOption(Long id, Long agendaId, String summary, String details) {
		this.id = id;
		this.agendaId = agendaId;
		this.summary = summary;
		this.details = details;
	}

	public static SelectOption from(Agenda agenda, SelectOptionCreationDto creationDto) {
		return new SelectOption(null, agenda.getId(), creationDto.summary(), creationDto.details());
	}
}
