package com.example.vote_service.domain.dto;

import com.example.vote_service.domain.Agenda;
import com.example.vote_service.domain.SelectOption;

import java.time.LocalDate;
import java.util.List;

public record AgendaCreationResultDTO(String apartmentCode, String title, String details, LocalDate endDate,
									  List<String> selectOptionSummaryList) {

	public static AgendaCreationResultDTO from(Agenda agenda, List<SelectOption> selectOptionList) {
		return new AgendaCreationResultDTO(agenda.getApartmentCode(), agenda.getTitle(), agenda.getDetails(), agenda.getEndDate(),
				selectOptionList.stream().map(SelectOption::getSummary).toList());
	}
}
