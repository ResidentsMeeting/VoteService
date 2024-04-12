package com.example.vote_service.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agenda extends BaseEntity {

	private Long id;

	@Column
	private String apartmentCode;

	@Column
	private String title;

	@Column
	private String details;

	@Column
	private LocalDate endDate;

	@Column
	private boolean secret;

	protected Agenda(Long id, String apartmentCode, String title, String details, LocalDate endDate, boolean secret) {
		this.id = id;
		this.apartmentCode = apartmentCode;
		this.title = title;
		this.details = details;
		this.endDate = endDate;
		this.secret = secret;
	}


}
