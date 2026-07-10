package vip.gruhasti.sso.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class FlowLoggingAspect {

    @Around("within(vip.gruhasti.sso.controller..*) || within(vip.gruhasti.sso.service..*)")
    public Object logFlow(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        log.info(">> {}", signature);
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            log.info("<< {} ({} ms)", signature, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.warn("!! {} failed after {} ms: {}", signature, System.currentTimeMillis() - start, ex.toString());
            throw ex;
        }
    }
}
