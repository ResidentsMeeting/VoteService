package com.example.vote_service.repository.agenda;

import com.example.vote_service.domain.SelectOptionHistory;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SelectOptionHistoryRepository extends R2dbcRepository<SelectOptionHistory, Long> {
	@Query("SELECT select_option_history.count FROM select_option_history WHERE select_option_history.id = :selectOptionId")
	Mono<Integer> findCountById(Long selectOptionId);

	@Query("SELECT voter_ids FROM voter_ids WHERE voter_ids.select_option_history_id = :selectOptionId")
	Flux<Long> findVoterIdsById(Long selectOptionId);
}
