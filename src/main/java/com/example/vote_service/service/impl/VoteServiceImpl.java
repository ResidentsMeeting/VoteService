package com.example.vote_service.service.impl;

import com.example.vote_service.UserInfo;
import com.example.vote_service.domain.dto.AgendaVo;
import com.example.vote_service.domain.dto.VoteCreationDto;
import com.example.vote_service.domain.dto.VoteEvent;
import com.example.vote_service.domain.dto.VoteHistory;
import com.example.vote_service.exception.VoteException;
import com.example.vote_service.exception.VoteExceptionCode;
import com.example.vote_service.messagequeue.KafkaProducer;
import com.example.vote_service.messagequeue.MessageProduceResult;
import com.example.vote_service.repository.agenda.SelectOptionHistoryRepository;
import com.example.vote_service.repository.agenda.AgendaCustomRepository;
import com.example.vote_service.repository.vote.VoteCustomRepository;
import com.example.vote_service.service.VoteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class VoteServiceImpl implements VoteService {
	private final AgendaCustomRepository agendaCustomRepository;
	private final VoteCustomRepository voteRepository;
	private final SelectOptionHistoryRepository selectOptionHistoryRepository;
	private final KafkaProducer kafkaProducer;

	@Value("${event.delay}")
	private Integer eventDelay;

	public VoteServiceImpl(AgendaCustomRepository agendaCustomRepository,
						   VoteCustomRepository voteRepository,
						   SelectOptionHistoryRepository selectOptionHistoryRepository, KafkaProducer kafkaProducer) {
		this.agendaCustomRepository = agendaCustomRepository;
		this.voteRepository = voteRepository;
		this.selectOptionHistoryRepository = selectOptionHistoryRepository;
		this.kafkaProducer = kafkaProducer;
	}

	@Override
	@Transactional
	public Mono<MessageProduceResult> createVote(VoteCreationDto voteCreationDto) {
		return Mono.just(voteCreationDto)
				.flatMap(voteCreation -> agendaCustomRepository.findBySelectOptionId(voteCreation.selectOptionId()))
				.switchIfEmpty(Mono.error(new VoteException(VoteExceptionCode.AGENDA_NOT_FOUND)))
				.flatMap(this::checkApartmentCode)
				.flatMap(agendaVo -> {
					if (LocalDate.now().isAfter(agendaVo.endDate())) {
						return Mono.error(new VoteException(VoteExceptionCode.AFTER_VOTE_END_DATE));
					}
					return Mono.just(agendaVo);
				})
				.transformDeferredContextual((mono, contextView) -> {
					UserInfo userInfo = contextView.get("user");
					return mono.zipWith(Mono.just(userInfo));
				})
				.map(tuple -> VoteEvent.toEvent(voteCreationDto, tuple.getT2().id()))
				.flatMap(kafkaProducer::send);
		}

	@Override
	@Transactional(readOnly = true)
	public Mono<VoteHistory> getVoteHistory(Long agendaId) {
		return Mono.deferContextual(contextView -> {
			UserInfo userInfo = contextView.get("user");
			return voteRepository.findVoteHistoryByUserIdAndAgendaId(userInfo.id(), agendaId);
		}).switchIfEmpty(Mono.error(new VoteException(VoteExceptionCode.NO_VOTE)));
	}

	@Override
	public Flux<Integer> getSelectOptionVoteCount(Long agendaId, Long selectOptionId) {
		return agendaCustomRepository.findById(agendaId)
				.flatMap(this::checkApartmentCode)
				.flux()
				.flatMap(agendaVo -> {
					if (LocalDate.now().isAfter(agendaVo.endDate())) {
						return selectOptionHistoryRepository
								.findCountById(selectOptionId)
								.switchIfEmpty(voteRepository.findVoteCountOfSelectOptionId(selectOptionId))
								.switchIfEmpty(Mono.just(0));
					} else if(agendaVo.secret()) {
						return Mono.error(new VoteException(VoteExceptionCode.ONGOING_SECRET_VOTE));
					} else {
						return Flux.interval(Duration.ofSeconds(eventDelay))
								.flatMap(time ->
										voteRepository
												.findVoteCountOfSelectOptionId(selectOptionId)
												.switchIfEmpty(Mono.just(0)));
					}
				});
	}

	@Override
	public Flux<List<Long>> getListOfUserIdOfAgendaAndSelectOption(Long agendaId, Long selectOptionId) {
		return agendaCustomRepository.findById(agendaId)
				.flatMap(this::checkApartmentCode)
				.flux()
				.flatMap(agendaVo -> {
					if (agendaVo.secret()) {
						return Mono.error(new VoteException(VoteExceptionCode.SECRET_VOTE));
					} else if (LocalDate.now().isAfter(agendaVo.endDate())) {
						return selectOptionHistoryRepository.findVoterIdsById(selectOptionId)
								.collectList().switchIfEmpty(Mono.just(Collections.emptyList()));
					} else {
						return Flux.interval(Duration.ofSeconds(eventDelay)).flatMap(
									time -> voteRepository.findUserIdsBySelectOptionId(selectOptionId)
											.collectList().switchIfEmpty(Mono.just(Collections.emptyList())));
					}
				});
	}

	private Mono<AgendaVo> checkApartmentCode(AgendaVo apartmentCode) {
		return Mono.just(apartmentCode)
				.zipWith(Mono.deferContextual(contextView -> Mono.just((UserInfo) contextView.get("user"))))
				.flatMap(tuple -> {
					AgendaVo agendaVo = tuple.getT1();
					String userApartmentCode = tuple.getT2().address().apartmentCode();
					if (!agendaVo.apartmentCode().equals(userApartmentCode)) {
						return Mono.error(new VoteException(VoteExceptionCode.NO_RIGHT_FOR));
					} else {
						return Mono.just(tuple.getT1());
					}
				});
	}
}
