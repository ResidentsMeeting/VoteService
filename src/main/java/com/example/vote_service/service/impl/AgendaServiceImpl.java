package com.example.vote_service.service.impl;

import com.example.vote_service.UserInfo;
import com.example.vote_service.domain.AgendaHistory;
import com.example.vote_service.domain.dto.AgendaCreationDTO;
import com.example.vote_service.domain.dto.AgendaEvent;
import com.example.vote_service.domain.dto.AgendaVo;
import com.example.vote_service.exception.VoteException;
import com.example.vote_service.exception.VoteExceptionCode;
import com.example.vote_service.messagequeue.KafkaProducer;
import com.example.vote_service.messagequeue.MessageProduceResult;
import com.example.vote_service.repository.agenda.AgendaCustomRepository;
import com.example.vote_service.repository.agenda.AgendaHistoryCustomRepository;
import com.example.vote_service.service.AgendaService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AgendaServiceImpl implements AgendaService {

	private final KafkaProducer kafkaProducer;
	private final AgendaCustomRepository agendaCustomRepository;
	private final AgendaHistoryCustomRepository agendaHistoryCustomRepository;

	public AgendaServiceImpl(KafkaProducer kafkaProducer,
							 AgendaCustomRepository agendaCustomRepository,
							 AgendaHistoryCustomRepository agendaHistoryCustomRepository) {
		this.kafkaProducer = kafkaProducer;
		this.agendaCustomRepository = agendaCustomRepository;
		this.agendaHistoryCustomRepository = agendaHistoryCustomRepository;
	}

	@Override
	public Mono<MessageProduceResult> createAgenda(AgendaCreationDTO creationDTO) {
		return Mono.just(creationDTO)
				.transformDeferredContextual((mono, contextView) -> mono.zipWith(Mono.just(contextView.get("user"))))
				.mapNotNull(tuple -> {
					AgendaCreationDTO agendaCreationDTO = tuple.getT1();
					String apartmentCode = ((UserInfo) tuple.getT2()).address().apartmentCode();
					return AgendaEvent.from(agendaCreationDTO,apartmentCode);
				}).switchIfEmpty(Mono.error(() -> new VoteException(VoteExceptionCode.NO_RIGHT_FOR)))
				.flatMap(kafkaProducer::send);
	}

	@Override
	public Mono<AgendaHistory> getAgendaHistory(Long agendaId) {
		return agendaHistoryCustomRepository.findById(agendaId)
				.switchIfEmpty(Mono.defer(() -> getAgendaHistoryMonoFromRepo(agendaId)))
				.transformDeferredContextual((mono, contextView) -> mono.zipWith(Mono.just(contextView.get("user"))))
				.flatMap(tuple -> {
					AgendaHistory agendaHistory = tuple.getT1();
					UserInfo userInfo = (UserInfo) tuple.getT2();
					if (userInfo.address().apartmentCode().equals(agendaHistory.getApartmentCode())) {
						return Mono.just(agendaHistory);
					} else {
						return Mono.empty();
					}
				}).switchIfEmpty(Mono.error(() -> new VoteException(VoteExceptionCode.NO_RIGHT_FOR)));

	}

	private Mono<AgendaHistory> getAgendaHistoryMonoFromRepo(Long agendaId) {
		return agendaCustomRepository.findByIdUsingFetchJoin(agendaId)
				.switchIfEmpty(Mono.error(() -> new VoteException(VoteExceptionCode.AGENDA_NOT_FOUND)))
				.map(AgendaVo::toAgendaHistory);
	}
}
