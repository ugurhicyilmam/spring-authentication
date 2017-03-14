package com.ugurhicyilmam.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggerAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggerAspect.class);

    private final boolean logAsInfo;

    public LoggerAspect(@Value("${application.log-service-layer-as-info}") boolean logAsInfo) {
        this.logAsInfo = logAsInfo;
    }

    @Before("execution(* com.ugurhicyilmam.service.*.*(..))")
    public void beforeServiceLayer(JoinPoint joinPoint) {
        String log = "Service: About to invoke " + joinPoint.getSignature().toShortString() + " with args " + Arrays.asList(joinPoint.getArgs());
        if (logAsInfo)
            logger.info(log);
        else
            logger.debug(log);
    }
}