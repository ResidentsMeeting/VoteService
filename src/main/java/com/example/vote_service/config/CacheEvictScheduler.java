package com.example.vote_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CacheEvictScheduler {

	private final CacheManager cacheManager;

	@Scheduled(timeUnit = TimeUnit.SECONDS, fixedDelay = 5)
	public void evictAllVoteCountCachesAtIntervals() {

		var cache = cacheManager.getCache("voteCount");
		if(cache != null) {
			cache.clear();
		}
	}

	@Scheduled(timeUnit = TimeUnit.SECONDS, fixedDelay = 5)
	public void evictAllVotersIdsCachesAtIntervals() {
		var cache = cacheManager.getCache("voteUserIds");
		if(cache != null) {
			cache.clear();
		}
	}
}
