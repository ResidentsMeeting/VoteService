package com.example.vote_service.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;


@Getter
public class SelectOptionHistory {
	private String summary;
	private String details;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer count;

	@Builder
	public SelectOptionHistory(String summary, String details, Integer count) {
		this.summary = summary;
		this.details = details;
		this.count = count;
	}
}
