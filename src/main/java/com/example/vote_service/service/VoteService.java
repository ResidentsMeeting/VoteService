package com.example.vote_service.service;


import com.example.vote_service.domain.dto.VoteCreationDto;
import com.example.vote_service.domain.dto.VoteHistory;
import com.example.vote_service.messagequeue.MessageProduceResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface VoteService {
	Mono<MessageProduceResult> createVote(VoteCreationDto voteCreationDto);

	Mono<VoteHistory> getVoteHistory(Long agendaId);

	Flux<Integer> getSelectOptionVoteCount(Long agendaId, Long selectOptionId);

	Flux<List<Long>> getListOfUserIdOfAgendaAndSelectOption(Long agendaId, Long selectOptionId);
}
