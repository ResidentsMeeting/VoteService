package com.example.vote_service.otherservice;

import reactor.core.publisher.Mono;

import java.util.List;

public interface UserService {

	Mono<List> getUserEmails(List<Long> userIds);
}
