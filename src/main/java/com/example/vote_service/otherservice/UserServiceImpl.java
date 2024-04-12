package com.example.vote_service.otherservice;

import com.example.vote_service.domain.AuthTokenHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
	@Value("${user-service-url}")
	private String userServiceUrl;
	private final WebClient webClient;
	private final AuthTokenHolder authTokenHolder;

	public UserServiceImpl(WebClient webClient, AuthTokenHolder authTokenHolder) {
		this.webClient = webClient;
		this.authTokenHolder = authTokenHolder;
	}

	@Override
	public Mono<List> getUserEmails(List<Long> userIds) {
		String userIdsString = userIds.stream().map(String::valueOf)
				.collect(Collectors.joining(","));

		return webClient.get()
				.uri(userServiceUrl+"/api/user/" + userIdsString)
				.header("Authorization", authTokenHolder.getToken())
				.retrieve()
				.bodyToMono(List.class);
	}
}
