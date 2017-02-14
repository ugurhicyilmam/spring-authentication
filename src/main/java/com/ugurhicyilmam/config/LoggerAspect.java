package com.ugurhicyilmam.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggerAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggerAspect.class);

    @Before("execution(* com.ugurhicyilmam.service.*.*(..))")
    public void beforeServiceLayer(JoinPoint joinPoint) {
        logger.debug("Service: About to invoke " + joinPoint.getSignature().toShortString() + " with args " + Arrays.asList(joinPoint.getArgs()));
    }
}
