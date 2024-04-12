package com.example.vote_service.controller;

import com.example.vote_service.domain.dto.VoteCreationDto;
import com.example.vote_service.domain.dto.VoteHistory;
import com.example.vote_service.filter.Authorize;
import com.example.vote_service.messagequeue.MessageProduceResult;
import com.example.vote_service.otherservice.UserService;
import com.example.vote_service.service.VoteService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static com.example.vote_service.UserInfo.UserRole.HOUSE_LEADER;
import static com.example.vote_service.UserInfo.UserRole.MEMBER;

@RestController
@RequestMapping("/api/vote")
public class VoteController {
	private final VoteService voteService;
	private final UserService userService;

	public VoteController(VoteService voteService, UserService userService) {
		this.voteService = voteService;
		this.userService = userService;
	}

	@Authorize(role = HOUSE_LEADER)
	@PostMapping("/")
	public Mono<MessageProduceResult> vote(@RequestBody @Validated VoteCreationDto creationDto) {
		return voteService.createVote(creationDto);
	}

	@Authorize(role = MEMBER)
	@GetMapping("agenda/{agendaId}")
	public Mono<VoteHistory> getCurrentVote(@PathVariable Long agendaId) {
		return voteService.getVoteHistory(agendaId);
	}

	@Authorize(role = MEMBER)
	@GetMapping(value = "/agenda/{agendaId}/select-option/{selectOptionId}/count", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Integer> getSelectOptionVoteCount(@PathVariable Long agendaId, @PathVariable Long selectOptionId) {
		return voteService.getSelectOptionVoteCount(agendaId, selectOptionId).log();
	}

	@Authorize(role = MEMBER)
	@GetMapping(value = "/agenda/{agendaId}/select-option/{selectOptionId}/info", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<List> getListOfUserOfSelectOption(@PathVariable Long agendaId, @PathVariable Long selectOptionId) {

		return voteService.getListOfUserIdOfAgendaAndSelectOption(agendaId, selectOptionId)
				.flatMap(userIds -> {
					if (userIds.isEmpty()) {
						return Mono.just(Collections.emptyList());
					}
					return userService.getUserEmails(userIds);
				});
	}
}
