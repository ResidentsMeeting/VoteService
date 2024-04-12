package com.example.vote_service.domain.dto;

import com.example.vote_service.domain.SelectOptionHistory;
import lombok.Builder;

@Builder
public record SelectOptionVo(
		Long id,
		String summary,
		String details){
	public SelectOptionHistory toSelectOptionHistory() {

		return SelectOptionHistory.builder()
				.summary(summary)
				.details(details)
				.build();
	}
}
