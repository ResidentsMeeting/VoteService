package com.example.vote_service.controller;

import com.example.vote_service.domain.AgendaHistory;
import com.example.vote_service.domain.dto.AgendaCreationDTO;
import com.example.vote_service.filter.Authorize;
import com.example.vote_service.messagequeue.MessageProduceResult;
import com.example.vote_service.service.AgendaService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static com.example.vote_service.UserInfo.UserRole.MEMBER;
import static com.example.vote_service.UserInfo.UserRole.VICE_LEADER;

@RestController
@RequestMapping("/api/agenda")
public class AgendaController {

	private final AgendaService agendaService;

	public AgendaController(AgendaService agendaService) {
		this.agendaService = agendaService;
	}

	@Authorize(role = VICE_LEADER)
	@PostMapping(value = "/")
	public Mono<MessageProduceResult> createAgenda(
			@RequestBody @Validated AgendaCreationDTO creationDTO) {
		return agendaService.createAgenda(creationDTO);
	}

	@Authorize(role = MEMBER)
	@GetMapping(value = "/{agendaId}")
	public Mono<AgendaHistory> getAgendaHistory(@PathVariable Long agendaId) {
		return agendaService.getAgendaHistory(agendaId);
	}
}
