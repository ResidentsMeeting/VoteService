package com.example.vote_service.repository.agenda;

import com.example.vote_service.domain.dto.AgendaVo;
import reactor.core.publisher.Mono;

public interface AgendaCustomRepository {
	Mono<AgendaVo> findById(Long id);
	Mono<AgendaVo> findByIdUsingFetchJoin(Long id);

	Mono<AgendaVo> findBySelectOptionId(Long selectOptionId);
}
