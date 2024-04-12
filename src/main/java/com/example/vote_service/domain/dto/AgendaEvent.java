package com.example.vote_service.domain.dto;

import com.example.vote_service.messagequeue.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AgendaEvent extends Event {

	private AgendaEvent(AgendaPayload agendaPayload) {
		super("agenda_sink", agendaPayload);
	}

	public static AgendaEvent from(AgendaCreationDTO creationDTO, String apartmentCode) {
		return new AgendaEvent(new AgendaPayload(creationDTO, apartmentCode));
	}

	private record AgendaPayload(
			Long id,
			String apartmentCode,
			String title,
			String details,
			LocalDate endDate,
			Boolean secret,
			LocalDateTime createdAt,
			List<SelectOptionPayload> selectOptionPayloadList

	) {
		public AgendaPayload(AgendaCreationDTO creationDTO, String apartmentCode) {
			this(null,
					apartmentCode,
					creationDTO.title(),
					creationDTO.details(),
					creationDTO.endDate(),
					creationDTO.secret(),
					LocalDateTime.now(),
					creationDTO.selectOptionCreationDtoList().stream()
							.map(SelectOptionPayload::new).toList());
		}

	}

	private record SelectOptionPayload(
			Long id,
			String summary,
			String details,
			LocalDateTime createdAt
	){
		public SelectOptionPayload(SelectOptionCreationDto creationDto) {
			this(null, creationDto.summary(), creationDto.details(), LocalDateTime.now());
		}
	}
}
