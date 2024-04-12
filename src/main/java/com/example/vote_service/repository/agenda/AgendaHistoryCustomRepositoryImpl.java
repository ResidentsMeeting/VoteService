package com.example.vote_service.repository.agenda;

import com.example.vote_service.domain.AgendaHistory;
import com.example.vote_service.domain.SelectOptionHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@RequiredArgsConstructor
@Repository
public class AgendaHistoryCustomRepositoryImpl implements AgendaHistoryCustomRepository {

	private final DatabaseClient databaseClient;

	//TODO : 결과가 없을 때 null 반환
	@Override
	public Mono<AgendaHistory> findById(Long id) {

		String sql = """
				SELECT a.id AS agendaId, a.apartment_code AS apartmentCode, a.title AS agendaTitle, a.secret as agendaSecret,
				a.details AS agendaDetails, a.end_date AS agendaEndDate, a.created_at AS agendaCreatedAt,
				s.id AS selectOptionId, s.summary AS selectOptionSummary,
    			s.details AS selectOptionDetails, s.count as selectOptionCount
				FROM agenda_history a 
				JOIN select_option_history s
				on a.id = s.agenda_id
				WHERE a.id = :id
				""";


		return databaseClient.sql(sql)
				.bind("id", id)
				.fetch().all()
				.collectList()
				.map(result -> {
					var selectOptions = result.stream()
							.map(row -> new SelectOptionHistory(
									(String) row.get("selectOptionSummary"),
									(String) row.get("selectOptionDetails"),
									(Integer) row.get("selectOptionCount"))
							)
							.toList();
					var row = result.get(0);

					return AgendaHistory.builder()
							.id((Long) row.get("agendaId"))
							.apartmentCode((String) row.get("apartmentCode"))
							.title((String) row.get("agendaTitle"))
							.details((String) row.get("agendaDetails"))
							.endDate((LocalDate) row.get("agendaEndDate"))
							.secret(row.get("agendaSecret") == null || (row.get("agendaSecret")).equals(1))
							.createdAt(row.get("agendaCreatedAt") == null ?
									null : ((ZonedDateTime) row.get("agendaCreatedAt")).toLocalDateTime())
							.selectOptions(selectOptions)
							.build();
				});
	}
}
