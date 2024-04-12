package com.example.vote_service.repository.agenda;

import com.example.vote_service.domain.AgendaHistory;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendaHistoryRepository extends R2dbcRepository<AgendaHistory, Long> {
}
