package com.example.vote_service;

import lombok.Getter;

public record UserInfo(Long id, String email, String name, String phone, Address address, UserRole userRole) {

	public record Address(String apartmentCode, int building, int unit) {}
	@Getter
	public enum UserRole {
		ADMIN(0),
		LEADER(1),
		VICE_LEADER(2),
		HOUSE_LEADER(3),
		MEMBER(4),
		UNREGISTERED(5);

		private final int priority;

		UserRole(int priority) {
			this.priority = priority;
		}
	}
}

