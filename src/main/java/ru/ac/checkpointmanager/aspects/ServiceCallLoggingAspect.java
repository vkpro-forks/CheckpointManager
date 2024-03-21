package ru.ac.checkpointmanager.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Aspect
@Slf4j
public class ServiceCallLoggingAspect {

    /**
     * Pointcut для перехвата вызова публичных методов из сервисов
     */
    @Pointcut("execution(public * ru.ac.checkpointmanager.service..*.*(..))")
    public void callAtServicesPublicMethods() {/*body unnecessary*/}

    @Before("callAtServicesPublicMethods()")
    public void beforeCallAnyMethod(JoinPoint joinPoint) {
        String args = Arrays.stream(joinPoint.getArgs())
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(","));
        log.debug("Call method " + joinPoint.getSignature() + ", args=[" + args + "]");
    }
}