package com.example.vote_service.service.impl;

import com.example.vote_service.UserInfo;
import com.example.vote_service.domain.dto.*;
import com.example.vote_service.exception.VoteException;
import com.example.vote_service.exception.VoteExceptionCode;
import com.example.vote_service.messagequeue.KafkaProducer;
import com.example.vote_service.messagequeue.MessageProduceResult;
import com.example.vote_service.repository.agenda.SelectOptionHistoryRepository;
import com.example.vote_service.repository.agenda.AgendaCustomRepository;
import com.example.vote_service.repository.vote.VoteCustomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class VoteServiceImplTest {
	@Mock
	private AgendaCustomRepository agendaCustomRepository;
	@Mock
	private VoteCustomRepository voteRepository;
	@Mock
	private SelectOptionHistoryRepository selectOptionHistoryRepository;

	@Mock
	private KafkaProducer kafkaProducer;
	@InjectMocks
	private VoteServiceImpl voteService;

	@Test
	@DisplayName("투표 생성 성공 - KafkaProducer 에서 성공하면 true 를 반환한다")
	void createVoteSuccess() {
		//given

		Long selectOptionId = 1L;
		Long userId = 2L;
		Long agendaId = 3L;
		String apartmentCode = "A12345678";

		VoteCreationDto voteCreationDto = new VoteCreationDto(selectOptionId);

		given(agendaCustomRepository.findBySelectOptionId(selectOptionId))
				.willReturn(Mono.just(new AgendaVo(1L,
						"A12345678",
						"제목",
						"설명",
						LocalDate.now().plusDays(3),
						true,
						null,
						List.of(new SelectOptionVo(1L,"찬성","찬성입니다."),
								new SelectOptionVo(2L,"반대","반대입니다.")))
						));

		given(kafkaProducer.send(any()))
				.willReturn(Mono.just(new MessageProduceResult(VoteEvent.toEvent(voteCreationDto, userId))));

		UserInfo userInfo =  new UserInfo(userId,
				null,
				null,
				null,
				new UserInfo.Address(apartmentCode,0,0),
				null);

		//when & then
		StepVerifier.create(voteService.createVote(voteCreationDto).contextWrite(
						context -> context.put("user",userInfo)
				))
				.expectNextMatches(messageProduceResult -> messageProduceResult.getStatus())
				.verifyComplete();
	}

	@Test
	@DisplayName("투표 생성 실패 - KafkaProducer 에서 실패하면 false 를 반환한다")
	void createVoteFail_FromKafkaProducer() {
		//given

		Long selectOptionId = 1L;
		Long userId = 2L;
		Long agendaId = 3L;
		String apartmentCode = "A12345678";

		given(agendaCustomRepository.findBySelectOptionId(selectOptionId))
				.willReturn(Mono.just(new AgendaVo(1L,
						"A12345678",
						"제목",
						"설명",
						LocalDate.now().plusDays(3),
						true,
						null,
						List.of(new SelectOptionVo(1L, "찬성","찬성입니다."),
								new SelectOptionVo(2L, "반대","반대입니다.")))
				));


		VoteCreationDto voteCreationDto = new VoteCreationDto(selectOptionId);

		given(kafkaProducer.send(any()))
				.willReturn(Mono.just(new MessageProduceResult(VoteEvent.toEvent(voteCreationDto, userId),
						new RuntimeException())));

		UserInfo userInfo =  new UserInfo(userId,
				null,
				null,
				null,
				new UserInfo.Address(apartmentCode,0,0),
				null);


		//when & then
		StepVerifier.create(voteService.createVote(voteCreationDto).contextWrite(
						context -> context.put("user", userInfo
						)))
				.expectNextMatches(messageProduceResult -> !messageProduceResult.getStatus())
				.verifyComplete();
	}

	@Test
	@DisplayName("투표 생성 실패 - 선택지를 찾을 수 없음")
	void createVoteFail_SelectOptionNotFound() {
		//given
		Long selectOptionId = 1L;
		Long userId = 2L;
		String apartmentCode = "A12345678";

		VoteCreationDto voteCreationDto = new VoteCreationDto(selectOptionId);

		UserInfo userInfo =  new UserInfo(userId,
				null,
				null,
				null,
				new UserInfo.Address(apartmentCode,0,0),
				null);

		given(agendaCustomRepository.findBySelectOptionId(selectOptionId))
				.willReturn(Mono.empty());

		//when & then
		StepVerifier.create(voteService.createVote(voteCreationDto).contextWrite(
						context -> context.put("user", userInfo
						)))
				.expectErrorMatches(throwable ->
						throwable instanceof VoteException &&
						throwable.getMessage().equals(VoteExceptionCode.AGENDA_NOT_FOUND.getMessage()))
				.verify();
	}

	@Test
	@DisplayName("투표 생성 실패 - 해당 아파트 주민이 아님")
	void createVoteFail_NoRightForVote() {
		//given
		Long selectOptionId = 1L;
		Long userId = 2L;
		Long agendaId = 3L;
		String apartmentCode = "A12345678";

		given(agendaCustomRepository.findBySelectOptionId(selectOptionId))
				.willReturn(Mono.just(new AgendaVo(1L,
						"A87654321",
						"제목",
						"설명",
						LocalDate.now().plusDays(3),
						true,
						null,
						List.of(new SelectOptionVo(1L, "찬성","찬성입니다."),
								new SelectOptionVo(2L, "반대","반대입니다.")))
				));

		VoteCreationDto voteCreationDto = new VoteCreationDto(selectOptionId);

		UserInfo userInfo =  new UserInfo(userId,
				null,
				null,
				null,
				new UserInfo.Address(apartmentCode,0,0),
				null);

		//when & then
		StepVerifier.create(voteService.createVote(voteCreationDto).contextWrite(
						context -> context.put("user", userInfo
						)))
				.expectErrorMatches(throwable ->
						throwable instanceof VoteException &&
								throwable.getMessage().equals(VoteExceptionCode.NO_RIGHT_FOR.getMessage()))
				.verify();
	}

	@Test
	@DisplayName("투표 생성 실패 - 투표 기한이 지남")
	void createVoteFail_AfterEndDate() {
		//given
		Long selectOptionId = 1L;
		Long userId = 2L;
		Long agendaId = 3L;
		String apartmentCode = "A12345678";

		given(agendaCustomRepository.findBySelectOptionId(selectOptionId))
				.willReturn(Mono.just(new AgendaVo(1L,
						apartmentCode,
						"제목",
						"설명",
						LocalDate.now().minusDays(1),
						true,
						null,
						List.of(new SelectOptionVo(1L, "찬성","찬성입니다."),
								new SelectOptionVo(2L, "반대","반대입니다.")))
				));

		VoteCreationDto voteCreationDto = new VoteCreationDto(selectOptionId);

		UserInfo userInfo =  new UserInfo(userId,
				null,
				null,
				null,
				new UserInfo.Address(apartmentCode,0,0),
				null);

		//when & then
		StepVerifier.create(voteService.createVote(voteCreationDto).contextWrite(
						context -> context.put("user", userInfo
						)))
				.expectErrorMatches(throwable ->
						throwable instanceof VoteException &&
								throwable.getMessage().equals(VoteExceptionCode.AFTER_VOTE_END_DATE.getMessage()))
				.verify();
	}


	@Test
	@DisplayName("투표 이력 가져오기 성공")
	void getVoteHistorySuccess() {
		//given
		Long selectOptionId = 1L;
		Long userId = 2L;
		Long agendaId = 3L;
		String ApartmentCode = "A12345678";
		VoteHistory voteHistory =
				new VoteHistory(selectOptionId,
						LocalDateTime.of(2021, 1, 1, 1, 1, 1));

		given(voteRepository.findVoteHistoryByUserIdAndAgendaId(userId,agendaId)).willReturn(
				Mono.just(voteHistory));

		UserInfo userInfo =  new UserInfo(userId,
				null,
				null,
				null,
				new UserInfo.Address(ApartmentCode,0,0),
				null);

		//when & then
		StepVerifier.create(voteService.getVoteHistory(agendaId).contextWrite(
						context -> context.put("user", userInfo
						)))
				.expectNext(voteHistory)
				.verifyComplete();

	}

	@Test
	@DisplayName("투표 이력 가져오기 성공 - 이력이 없음")
	void getVoteHistorySuccess_EmptyHistory() {
		//given
		Long userId = 2L;
		Long agendaId = 3L;
		String ApartmentCode = "A12345678";

		given(voteRepository.findVoteHistoryByUserIdAndAgendaId(userId,agendaId)).willReturn(
				Mono.empty());

		UserInfo userInfo =  new UserInfo(userId,
				null,
				null,
				null,
				new UserInfo.Address(ApartmentCode,0,0),
				null);

		//when & then
		StepVerifier.create(voteService.getVoteHistory(agendaId).contextWrite(
						context -> context.put("user", userInfo
						)))
				.expectErrorMatches(throwable ->
						throwable instanceof VoteException &&
								throwable.getMessage().equals(VoteExceptionCode.NO_VOTE.getMessage()))
				.verify();

	}

	@Test
	@DisplayName("선택지 투표수 가져오기 성공 - 투표가 종료된 경우")
	void getSelectOptionVoteCountSuccess() {
		//given
		Long userId = 2L;
		Long agendaId = 3L;
		Long selectOptionId = 4L;
		Integer voteCount = 158347;
		String apartmentCode = "A12345678";

		given(agendaCustomRepository.findById(agendaId)).willReturn(
				Mono.just(new AgendaVo(agendaId,
						apartmentCode,
						"제목",
						"설명",
						LocalDate.now().minusDays(1),
						true,
						null,
						Collections.emptyList())));

		given(selectOptionHistoryRepository.findCountById(selectOptionId))
				.willReturn(Mono.just(voteCount));

		given(voteRepository.findVoteCountOfSelectOptionId(selectOptionId))
				.willReturn(Mono.empty());

		UserInfo userInfo =  new UserInfo(userId,
				null,
				null,
				null,
				new UserInfo.Address(apartmentCode,0,0),
				null);

		//when & then
		StepVerifier.create(voteService.getSelectOptionVoteCount(agendaId,selectOptionId).contextWrite(
						context -> context.put("user", userInfo
						)))
				.expectNext(voteCount)
				.verifyComplete();

	}

	@Test
	@DisplayName("선택지 투표수 가져오기 실패 - 끝나지 않은 비밀 투표")
	void getSelectOptionVoteCountFail_OngoingSecretVote() {
		//given
		Long userId = 2L;
		Long agendaId = 3L;
		Long selectOptionId = 4L;
		String apartmentCode = "A12345678";

		given(agendaCustomRepository.findById(agendaId)).willReturn(
				Mono.just(new AgendaVo(agendaId,
						apartmentCode,
						"제목",
						"설명",
						LocalDate.now(),
						true,
						null,
						Collections.emptyList())));

		UserInfo userInfo =  new UserInfo(userId,
				null,
				null,
				null,
				new UserInfo.Address(apartmentCode,0,0),
				null);

		//when & then
		StepVerifier.create(voteService.getSelectOptionVoteCount(agendaId,selectOptionId).contextWrite(
						context -> context.put("user", userInfo
						)))
				.expectErrorMatches(throwable ->
						throwable instanceof VoteException &&
								throwable.getMessage().equals(VoteExceptionCode.ONGOING_SECRET_VOTE.getMessage()))
				.verify();

	}

	@Test
	@DisplayName("선택지 투표수 가져오기 성공 - 진행중인 공개 투표")
	void getListOfUserIdsOfSelectOptionSuccess_OngoingRevealedVote() {
		//given
		Long userId = 2L;
		Long agendaId = 3L;
		Long selectOptionId = 4L;
		Integer voteCount = 158347;
		String apartmentCode = "A12345678";

		given(agendaCustomRepository.findById(agendaId)).willReturn(
				Mono.just(new AgendaVo(agendaId,
						apartmentCode,
						"제목",
						"설명",
						LocalDate.now(),
						false,
						null,
						Collections.emptyList())));

		given(voteRepository.findVoteCountOfSelectOptionId(selectOptionId))
				.willReturn(Mono.just(voteCount));

		UserInfo userInfo =  new UserInfo(userId,
				null,
				null,
				null,
				new UserInfo.Address(apartmentCode,0,0),
				null);

		//when & then
		StepVerifier.withVirtualTime(() -> voteService.getSelectOptionVoteCount(agendaId,selectOptionId).contextWrite(
						context -> context.put("user", userInfo
						)))
				.expectSubscription()
				.then(() -> VirtualTimeScheduler.get().advanceTimeBy(Duration.ofSeconds(2*5)))
				.expectNext(voteCount)
				.expectNextCount(4)
				.thenCancel()
				.verify();
	}

	@Test
	@DisplayName("선택지 투표수 가져오기 실패 - 해당 아파트 주민이 아님")
	void getListOfUserIdsOfSelectOptionFail_ApartmentCodeNotMatch() {
		//given
		Long userId = 2L;
		Long agendaId = 3L;
		Long selectOptionId = 4L;
		String apartmentCode = "A12345678";

		given(agendaCustomRepository.findById(agendaId)).willReturn(
				Mono.just(new AgendaVo(agendaId,
						"A87654321",
						"제목",
						"설명",
						LocalDate.now(),
						false,
						null,
						Collections.emptyList())));

		UserInfo userInfo =  new UserInfo(userId,
				null,
				null,
				null,
				new UserInfo.Address(apartmentCode,0,0),
				null);

		//when & then
		StepVerifier.create(voteService.getSelectOptionVoteCount(agendaId,selectOptionId).contextWrite(
						context -> context.put("user", userInfo
						)))
				.expectErrorMatches(throwable ->
						throwable instanceof VoteException &&
								throwable.getMessage().equals(VoteExceptionCode.NO_RIGHT_FOR.getMessage()))
				.verify();
	}


	@Test
	@DisplayName("선택지 user id 가져오기 성공 - 투표가 종료된 경우")
	void getListOfUserIdsOfSelectOptionSuccess_AfterEndDate() {
		//given
		Long userId = 2L;
		Long agendaId = 3L;
		Long selectOptionId = 4L;
		String apartmentCode = "A12345678";

		given(agendaCustomRepository.findById(agendaId)).willReturn(
				Mono.just(new AgendaVo(agendaId,
						apartmentCode,
						"제목",
						"설명",
						LocalDate.now().minusDays(1),
						false,
						null,
						Collections.emptyList())));

		given(selectOptionHistoryRepository.findVoterIdsById(selectOptionId))
				.willReturn(Flux.fromIterable(List.of(1L,3L,5L)));

		UserInfo userInfo =  new UserInfo(userId,
				null,
				null,
				null,
				new UserInfo.Address(apartmentCode,0,0),
				null);

		//when & then
		StepVerifier.create(voteService.getListOfUserIdOfAgendaAndSelectOption(agendaId,selectOptionId).contextWrite(
						context -> context.put("user", userInfo
						)))
				.expectNextMatches(list -> list.containsAll(List.of(1L,3L,5L)) && list.size() == 3)
				.verifyComplete();

	}

	@Test
	@DisplayName("선택지 user id 가져오기 실패 - 비밀 투표")
	void getSelectOptionVoteCountFail_SecretVote() {
		//given
		Long userId = 2L;
		Long agendaId = 3L;
		Long selectOptionId = 4L;
		String apartmentCode = "A12345678";

		given(agendaCustomRepository.findById(agendaId)).willReturn(
				Mono.just(new AgendaVo(agendaId,
						apartmentCode,
						"제목",
						"설명",
						LocalDate.now(),
						true,
						null,
						Collections.emptyList())));

		UserInfo userInfo =  new UserInfo(userId,
				null,
				null,
				null,
				new UserInfo.Address(apartmentCode,0,0),
				null);

		//when & then
		StepVerifier.create(voteService.getListOfUserIdOfAgendaAndSelectOption(agendaId,selectOptionId).contextWrite(
						context -> context.put("user", userInfo
						)))
				.expectErrorMatches(throwable ->
						throwable instanceof VoteException &&
								throwable.getMessage().equals(VoteExceptionCode.SECRET_VOTE.getMessage()))
				.verify();

	}

	@Test
	@DisplayName("선택지 user id 가져오기 성공  - 진행중인 공개 투표")
	void getSelectOptionVoteCountSuccess_OngoingRevealedVote() {
		//given
		Long userId = 2L;
		Long agendaId = 3L;
		Long selectOptionId = 4L;
		String apartmentCode = "A12345678";

		given(agendaCustomRepository.findById(agendaId)).willReturn(
				Mono.just(new AgendaVo(agendaId,
						apartmentCode,
						"제목",
						"설명",
						LocalDate.now(),
						false,
						null,
						Collections.emptyList())));

		given(voteRepository.findUserIdsBySelectOptionId(selectOptionId))
				.willReturn(Flux.fromIterable(List.of(1L,3L,5L)));

		UserInfo userInfo =  new UserInfo(userId,
				null,
				null,
				null,
				new UserInfo.Address(apartmentCode,0,0),
				null);

		//when & then
		StepVerifier.withVirtualTime(() -> voteService.getListOfUserIdOfAgendaAndSelectOption(agendaId,selectOptionId).contextWrite(
						context -> context.put("user", userInfo
						)))
				.expectSubscription()
				.then(() -> VirtualTimeScheduler.get().advanceTimeBy(Duration.ofSeconds(2*5)))
				.expectNextMatches(list -> list.containsAll(List.of(1L,3L,5L)) && list.size() == 3)
				.expectNextCount(4)
				.thenCancel()
				.verify();
	}
}