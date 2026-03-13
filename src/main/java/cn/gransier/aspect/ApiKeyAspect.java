package cn.gransier.aspect;

import cn.gransier.context.ApiKeyContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class ApiKeyAspect {

    @Around("@within(cn.gransier.annotation.AgentService)")
    public Object setAndClearApiKey(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            String apiKey = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                apiKey = authHeader.substring(7);
            }
            ApiKeyContext.setApiKey(apiKey);
            return joinPoint.proceed();
        } finally {
            ApiKeyContext.clear();
        }
    }
}