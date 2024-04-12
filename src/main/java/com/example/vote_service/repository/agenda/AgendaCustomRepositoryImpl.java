package com.example.vote_service.repository.agenda;

import com.example.vote_service.domain.dto.AgendaVo;
import com.example.vote_service.domain.dto.SelectOptionVo;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Repository
public class AgendaCustomRepositoryImpl implements AgendaCustomRepository {

	private final DatabaseClient databaseClient;

	public AgendaCustomRepositoryImpl(DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
	}

	@Override
	public Mono<AgendaVo> findById(Long id) {

		String sql = """
				SELECT a.id AS agendaId, a.apartment_code AS apartmentCode, a.title AS agendaTitle, 
    			a.details AS agendaDetails, a.end_date AS agendaEndDate, a.secret as agendaSecret, a.created_at AS agendaCreatedAt
				FROM agenda a 
				WHERE a.id = :id
				""";

		return databaseClient.sql(sql)
				.bind("id", id)
				.fetch().first().map( result -> AgendaVo.builder()
						.id((Long) result.get("agendaId"))
						.apartmentCode((String) result.get("apartmentCode"))
						.title((String) result.get("agendaTitle"))
						.details((String) result.get("agendaDetails"))
						.endDate((LocalDate) result.get("agendaEndDate"))
						.secret(result.get("agendaSecret") == null || (result.get("agendaSecret")).equals(1))
						.createdAt(result.get("agendaCreatedAt") == null ?
								null : ((ZonedDateTime) result.get("agendaCreatedAt")).toLocalDateTime())
						.build());
	}

	@Override
	public Mono<AgendaVo> findByIdUsingFetchJoin(Long id) {

		String sqlWithSelectOption =
    		"""
    			SELECT a.id AS agendaId, a.apartment_code AS apartmentCode, a.title AS agendaTitle, 
    			a.details AS agendaDetails, a.end_date AS agendaEndDate, a.secret as agendaSecret, a.created_at AS agendaCreatedTime,
    			s.id AS selectOptionId, s.summary AS selectOptionSummary,
    			s.details AS selectOptionDetails
				FROM agenda a 
				JOIN select_option s
				ON a.id = s.agenda_id
				WHERE a.id = :id
			""";

		return databaseClient.sql(sqlWithSelectOption)
				.bind("id", id)
				.fetch().all()
				.collectList()
				.map(result -> {
					var selectOptions = result.stream()
							.map(row -> new SelectOptionVo(
									(Long) row.get("selectOptionId"),
									(String) row.get("selectOptionSummary"),
									(String) row.get("selectOptionDetails")))
							.toList();
					var row = result.get(0);

					return AgendaVo.builder()
							.id((Long) row.get("agendaId"))
							.apartmentCode((String) row.get("apartmentCode"))
							.title((String) row.get("agendaTitle"))
							.details((String) row.get("agendaDetails"))
							.endDate((LocalDate) row.get("agendaEndDate"))
							.secret(row.get("agendaSecret") == null || (row.get("agendaSecret")).equals(1))
							.createdAt(row.get("agendaCreatedTime") == null ?
									null : ((ZonedDateTime) row.get("agendaCreatedTime")).toLocalDateTime())
							.selectOptionList(selectOptions)
							.build();
				});
	}

	@Override
	public Mono<AgendaVo> findBySelectOptionId(Long selectOptionId) {
		String sql = """
				SELECT a.id, a.apartment_code, a.title, a.details, a.end_date,a.secret, a.created_at
				FROM agenda a
				JOIN select_option s
				ON a.id = s.agenda_id
				WHERE s.id = :selectOptionId
				""";
		return databaseClient.sql(sql)
				.bind("selectOptionId", selectOptionId)
				.fetch().first()
				.map(result ->
						AgendaVo.builder()
						.id((Long) result.get("id"))
						.apartmentCode((String) result.get("apartment_code"))
						.title((String) result.get("title"))
						.details((String) result.get("details"))
						.endDate((LocalDate) result.get("end_date"))
						.secret(result.get("secret") == null || result.get("secret").equals(1))
						.createdAt(result.get("created_time") == null ?
								null : ((ZonedDateTime) result.get("created_time")).toLocalDateTime())
						.build());
	}


}
