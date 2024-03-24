package ru.ac.checkpointmanager.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Aspect
@Slf4j
@ConditionalOnProperty(name = "logging.aspect.call-service", havingValue = "true")
public class ServiceCallLoggingAspect {

    @Value("${logging.aspect.include-non-public:false}")
    private boolean includeNonPublicMethods;

    /**
     * Pointcut для перехвата вызова методов из сервисов
     */
    @Pointcut("execution(* ru.ac.checkpointmanager.service..*.*(..))")
    public void callAtServicesMethods() {
        //body unnecessary
    }

    /**
     * Логирует сигнатуру и параметры вызываемых методов перед их вызовом.
     * Логируются все публичные методы, а не публичные - в зависимости от свойства includeNonPublicMethods
     * (следует учитывать, что вызов методов внутри объекта не логируется вне зависимости от модификаторов доступа
     * вследствие использования прокси в Spring AOP, т.е. нельзя логировать приватные методы с таким подходом)
     *
     * @param joinPoint точка присоединения аспекта к коду сервиса
     */
    @Before("callAtServicesMethods()")
    public void logCallMethodWithArgs(JoinPoint joinPoint) {

        if (!includeNonPublicMethods &&
                !Modifier.isPublic(((MethodSignature) joinPoint.getSignature()).getMethod().getModifiers())) {
            return;
        }

        String args = Arrays.stream(joinPoint.getArgs())
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(","));
        log.debug("Call method " + joinPoint.getSignature() + ", args=[" + args + "]");
    }
}