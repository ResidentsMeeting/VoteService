package com.example.vote_service.filter;

import com.example.vote_service.UserInfo;
import com.example.vote_service.exception.VoteException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.example.vote_service.exception.VoteExceptionCode.NO_RIGHT_FOR;

@Aspect
@Component
public class SecurityAOP {
	@Around("@annotation(Authorize)")
	public Publisher<Object> checkSecurity(ProceedingJoinPoint point) throws Throwable {
		Mono<UserInfo> userInfoMono = Mono.deferContextual(ctx -> Mono.justOrEmpty((UserInfo)ctx.get("user")))
				.switchIfEmpty(Mono.error(new RuntimeException("User not found")))
				.filter(user -> {

					UserInfo.UserRole contextUserRole = user.userRole();
					MethodSignature signature = (MethodSignature) point.getSignature();
					UserInfo.UserRole limitUserRole= signature.getMethod().getAnnotation(Authorize.class).role();

					return contextUserRole.getPriority() <= limitUserRole.getPriority();
				}).switchIfEmpty(Mono.error(new VoteException(NO_RIGHT_FOR)));

		Object result = point.proceed();

		if(result instanceof Mono) {
			return userInfoMono.flatMap(userInfo -> (Mono<?>) result);
		} else if(result instanceof Flux) {
			return userInfoMono.flux().flatMap(userInfo -> (Flux<?>) result);
		} else {
			return Mono.error(new RuntimeException("Unknown return type"));
		}

	}
}
