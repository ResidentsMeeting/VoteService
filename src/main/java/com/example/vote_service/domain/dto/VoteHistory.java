package com.example.vote_service.domain.dto;

import java.time.LocalDateTime;

public record VoteHistory(Long selectOptionId, LocalDateTime voteTime) {
}
