package com.example.vote_service.domain;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

@Component
public class AuthTokenHolder extends RequestContextHolder {

	private static String token;

	public static String getToken() {
		return token;
	}

	public static void setToken(String token) {
		AuthTokenHolder.token = token;
	}
}
