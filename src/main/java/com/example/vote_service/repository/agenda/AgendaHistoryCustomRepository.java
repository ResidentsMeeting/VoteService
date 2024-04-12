package com.example.vote_service.repository.agenda;

import com.example.vote_service.domain.AgendaHistory;
import reactor.core.publisher.Mono;

public interface AgendaHistoryCustomRepository {

	Mono<AgendaHistory> findById(Long id);
}
