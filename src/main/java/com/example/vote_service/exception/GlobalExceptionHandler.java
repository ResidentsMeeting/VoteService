package com.example.vote_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public Mono<ResponseEntity<String>> handleException(Exception ex) {
		log.error("Unexpected error occurred: ", ex);
		return Mono.just(ResponseEntity.internalServerError().body("Unexpected error occurred: " + ex.getMessage()));
	}

	@ExceptionHandler(VoteException.class)
	public Mono<ResponseEntity<String>> handleVoteException(VoteException ex) {
			log.debug("Validation error occurred: ", ex);
		return Mono.just(ResponseEntity.badRequest().body(ex.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public Mono<ResponseEntity<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
			log.info("Validation error occurred: ", ex);
		return Mono.just(ResponseEntity.badRequest().body(ex.getMessage()));
	}
}