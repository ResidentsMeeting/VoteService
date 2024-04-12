package com.example.vote_service.repository.vote;

import com.example.vote_service.domain.dto.VoteHistory;
import com.example.vote_service.filter.ReactorCacheable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public class VoteCustomRepositoryImpl implements VoteCustomRepository {

	private final DatabaseClient databaseClient;

	public VoteCustomRepositoryImpl(DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
	}

	@Override
	public Mono<VoteHistory> findVoteHistoryByUserIdAndAgendaId(Long userId, Long agendaId) {

		String sql = """ 
			SELECT s.id as id, v.created_at as createdTime
			FROM vote as v
				INNER JOIN select_option s ON v.select_option_id = s.id
			WHERE s.agenda_id = :agendaId
				AND v.user_id = :userId
			ORDER BY v.created_at DESC
			LIMIT 1
			     """;

		return databaseClient.sql(sql)
				.bind("userId",userId)
				.bind("agendaId",agendaId)
				.map(row -> new VoteHistory(row.get("id", Long.class),
						row.get("createdTime", LocalDateTime.class)))
				.first();
	}

	@Override
	@ReactorCacheable(name = "voteCount")
	public Mono<Integer> findVoteCountOfSelectOptionId(Long selectOptionId) {

		String sql = """
			SELECT count(v.user_id) as voteCount
			FROM vote as v
			WHERE (v.user_id, v.created_at) IN (
				SELECT v2.user_id, MAX(v2.created_at)
				FROM vote v2
				WHERE v2.select_option_id IN (
					SELECT s.id FROM select_option s 
					WHERE s.agenda_id = (SELECT s2.agenda_id from select_option s2 
										WHERE s2.id = :selectOptionId ) 
				)
				GROUP BY v2.user_id
			) and v.select_option_id = :selectOptionId
			GROUP BY v.select_option_id
			""";

		return databaseClient.sql(sql)
				.bind("selectOptionId",selectOptionId)
				.map(row -> row.get("voteCount", Integer.class))
				.one()
				.log();
	}

	@Override
	@ReactorCacheable(name = "voteUserIds")
	public Flux<Long> findUserIdsBySelectOptionId(Long selectOptionId) {

		String sql = """
			SELECT v.user_id as userId
			FROM vote as v
			WHERE (v.user_id, v.created_at) IN (
				SELECT v2.user_id, MAX(v2.created_at)
				FROM vote v2
				WHERE v2.select_option_id IN (
					SELECT s.id FROM select_option s 
					WHERE s.agenda_id = (SELECT s2.agenda_id from select_option as s2 
										WHERE s2.id = :selectOptionId) 
				)
				GROUP BY v2.user_id
			)
			AND v.select_option_id = :selectOptionId
			 """;
		return databaseClient.sql(sql)
				.bind("selectOptionId",selectOptionId)
				.map(row -> row.get("userId", Long.class))
				.all()
				.log();
	}
}
