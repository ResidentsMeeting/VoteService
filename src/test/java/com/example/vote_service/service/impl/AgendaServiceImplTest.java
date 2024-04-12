package com.example.vote_service.service.impl;

import com.example.vote_service.UserInfo;
import com.example.vote_service.domain.AgendaHistory;
import com.example.vote_service.domain.SelectOptionHistory;
import com.example.vote_service.domain.dto.*;
import com.example.vote_service.exception.VoteException;
import com.example.vote_service.exception.VoteExceptionCode;
import com.example.vote_service.messagequeue.KafkaProducer;
import com.example.vote_service.messagequeue.MessageProduceResult;
import com.example.vote_service.repository.agenda.AgendaCustomRepository;
import com.example.vote_service.repository.agenda.AgendaHistoryCustomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AgendaServiceImplTest {
	@Mock
	private KafkaProducer kafkaProducer;
	@Mock
	private AgendaCustomRepository agendaCustomRepository;
	@Mock
	private AgendaHistoryCustomRepository agendaHistoryRepository;
	@InjectMocks
	private AgendaServiceImpl agendaService;

	@Test
	@DisplayName("안건 생성 성공 - KafkaProducer 에서 성공하면 true 를 반환한다")
	void createAgendaSuccess() {
		//given
		List<SelectOptionCreationDto> selectOptionCreationDtoList = List.of(
				new SelectOptionCreationDto("찬성", "찬성입니다."),
				new SelectOptionCreationDto("반대", "반대입니다.")
		);
		AgendaCreationDTO creationDTO = new AgendaCreationDTO(
				"제목",
				"설명",
				LocalDate.now().plusDays(3),
				true,
				selectOptionCreationDtoList
		);

		given(kafkaProducer.send(any(AgendaEvent.class)))
				.willReturn(Mono.just(new MessageProduceResult(AgendaEvent.from(creationDTO, "A12345678"))));

		//when & then
		StepVerifier.create(agendaService.createAgenda(creationDTO)
				.contextWrite(context -> context.put("user", new UserInfo(1L,
						null,
						null,
						null,
						new UserInfo.Address("A12345678", 0, 0),
						null))))
				.expectNextMatches(result -> result.getStatus())
				.verifyComplete();

	}

	@Test
	@DisplayName("안건 생성 실패 - KafkaProducer 에서 실패하면 false 를 반환한다")
	void createAgendaFail() {
		//given
		List<SelectOptionCreationDto> selectOptionCreationDtoList = List.of(
				new SelectOptionCreationDto("찬성", "찬성입니다."),
				new SelectOptionCreationDto("반대", "반대입니다.")
		);
		AgendaCreationDTO creationDTO = new AgendaCreationDTO(
				"제목",
				"설명",
				LocalDate.now().plusDays(3),
				true,
				selectOptionCreationDtoList
		);

		given(kafkaProducer.send(any(AgendaEvent.class)))
				.willReturn(Mono.just(new MessageProduceResult(AgendaEvent.from(creationDTO, "A12345678"), new RuntimeException())));

		//when & then
		StepVerifier.create(agendaService.createAgenda(creationDTO)
						.contextWrite(context -> context.put("user", new UserInfo(1L,
								null,
								null,
								null,
								new UserInfo.Address("A12345678", 0, 0),
								null))))
				.expectNextMatches(result -> !result.getStatus())
				.verifyComplete();
	}

	@Test
	@DisplayName("안건 이력 가져오기 성공 - AgendaHistoryRepository 에서 가져온다")
	void getAgendaHistorySuccess_FromAgendaHistoryRepo() {
		//given
		Long agendaId = 1L;
		String title = "안건 제목";
		String details = "안건 설명";

		AgendaHistory agendaHistory = AgendaHistory.builder()
				.apartmentCode("A12345678")
				.title(title)
				.details(details)
				.endDate(LocalDate.now().minusDays(3))
				.selectOptions(
						List.of(
								new SelectOptionHistory("찬성", "찬성입니다.", 1),
								new SelectOptionHistory("반대", "반대입니다.", 2)
						)
				)
				.build();

		given(agendaHistoryRepository.findById(agendaId))
				.willReturn(Mono.just(agendaHistory));

		StepVerifier.create(agendaService.getAgendaHistory(agendaId)
						.contextWrite(context -> context.put("user", new UserInfo(1L,
								null,
								null,
								null,
								new UserInfo.Address("A12345678", 0, 0),
								null))))
				.expectSubscription()
				.expectNext(agendaHistory)
				.verifyComplete();
	}

	@Test
	@DisplayName("안건 이력 가져오기 성공 - Agenda,SelectOption 에서 가져온다")
	void getAgendaHistorySuccess_FromAgendaRepo() {
		//given
		Long agendaId = 1L;
		String title = "안건 제목";
		String details = "안건 설명";

		given(agendaHistoryRepository.findById(agendaId))
				.willReturn(Mono.empty());

		given(agendaCustomRepository.findByIdUsingFetchJoin(agendaId))
				.willReturn(Mono.just(AgendaVo.builder()
								.id(agendaId)
								.apartmentCode("A12345678")
								.title(title)
								.details(details)
								.endDate(LocalDate.now().plusDays(3))
								.secret(false)
								.selectOptionList(
										List.of(
												new SelectOptionVo(1L,"찬성", "찬성입니다." ),
												new SelectOptionVo(2L,"반대", "반대입니다.")
										)
								)
						.build()));


		StepVerifier.create(agendaService.getAgendaHistory(agendaId)
						.contextWrite(context -> context.put("user", new UserInfo(1L,
						null,
						null,
						null,
						new UserInfo.Address("A12345678", 0, 0),
						null))))
				.consumeNextWith(agendaHistory -> {
					// 원하는 필드를 추출하고 검사
					assertEquals(agendaId, agendaHistory.getId());
					assertEquals("A12345678", agendaHistory.getApartmentCode());
					assertEquals(title, agendaHistory.getTitle());
					assertEquals(details, agendaHistory.getDetails());
					assertEquals(LocalDate.now().plusDays(3), agendaHistory.getEndDate());

					List<SelectOptionHistory> selectOptions = agendaHistory.getSelectOptions();
					assertEquals(2, selectOptions.size());
					assertEquals("찬성", selectOptions.get(0).getSummary());
					assertEquals("찬성입니다.", selectOptions.get(0).getDetails());

					assertEquals("반대", selectOptions.get(1).getSummary());
					assertEquals("반대입니다.", selectOptions.get(1).getDetails());
				})
				.verifyComplete();
	}



	@Test
	@DisplayName("안건 이력 가져오기 실패 - AgendaNotFound")
	void getAgendaHistoryFail_AgendaNotFound() {
		//given
		Long agendaId = 1L;
		given(agendaHistoryRepository.findById(agendaId))
				.willReturn(Mono.empty());

		given(agendaCustomRepository.findByIdUsingFetchJoin(agendaId))
				.willReturn(Mono.empty());

		StepVerifier
				.create(agendaService.getAgendaHistory(agendaId)
						.contextWrite(context -> context.put("user", new UserInfo(1L,
								null,
								null,
								null,
								new UserInfo.Address("A12345678", 0, 0),
								null))))
				.expectSubscription()
				.expectErrorMatches(throwable ->
						throwable instanceof VoteException &&
						throwable.getMessage().equals(VoteExceptionCode.AGENDA_NOT_FOUND.getMessage()))
				.verify();
	}

	@Test
	@DisplayName("안건 이력 가져오기 실패 - ApartmentCodeNotMatch")
	void getAgendaHistoryFail_OngoingSecretVote() {
		//given
		Long agendaId = 1L;
		String title = "안건 제목";
		String details = "안건 설명";

		AgendaHistory agendaHistory = AgendaHistory.builder()
				.apartmentCode("A12345678")
				.title(title)
				.details(details)
				.endDate(LocalDate.now().minusDays(3))
				.selectOptions(
						List.of(
								new SelectOptionHistory("찬성", "찬성입니다.", 1),
								new SelectOptionHistory("반대", "반대입니다.", 2)
						)
				)
				.build();

		given(agendaHistoryRepository.findById(agendaId))
				.willReturn(Mono.just(agendaHistory));

		StepVerifier
				.create(agendaService.getAgendaHistory(agendaId)
						.contextWrite(context -> context.put("user", new UserInfo(1L,
								null,
								null,
								null,
								new UserInfo.Address("A87654321", 0, 0),
								null))))
				.expectSubscription()
				.expectErrorMatches(throwable ->
						throwable instanceof VoteException &&
						throwable.getMessage().equals(VoteExceptionCode.NO_RIGHT_FOR.getMessage()))
				.verify();
	}
}