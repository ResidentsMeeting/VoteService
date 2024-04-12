package com.example.vote_service.repository.vote;

import com.example.vote_service.domain.dto.VoteHistory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VoteCustomRepository {

	Mono<VoteHistory> findVoteHistoryByUserIdAndAgendaId(Long userId, Long agendaId);
	Mono<Integer> findVoteCountOfSelectOptionId(Long selectOptionId);
	Flux<Long> findUserIdsBySelectOptionId(Long selectOptionId);
}
