package com.orderprocessing.inventory.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log =
            LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(com.orderprocessing.inventory.logging.LogExecution)")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {

        String className =
                joinPoint.getSignature().getDeclaringType().getSimpleName();

        String methodName =
                joinPoint.getSignature().getName();

        long startTime = System.currentTimeMillis();

        try {
            MDC.put("logEvent", "METHOD_START");
            MDC.put("className", className);
            MDC.put("methodName", methodName);

            log.info("Method execution started");

            Object result = joinPoint.proceed();

            long duration =
                    System.currentTimeMillis() - startTime;

            MDC.put("logEvent", "METHOD_SUCCESS");
            MDC.put("durationMs", String.valueOf(duration));

            log.info("Method execution completed");

            return result;

        } catch (Exception exception) {

            long duration =
                    System.currentTimeMillis() - startTime;

            MDC.put("logEvent", "METHOD_FAILED");
            MDC.put("durationMs", String.valueOf(duration));

            log.error(
                    "Method execution failed",
                    exception
            );

            throw exception;

        } finally {

            MDC.remove("logEvent");
            MDC.remove("className");
            MDC.remove("methodName");
            MDC.remove("durationMs");
        }
    }
}