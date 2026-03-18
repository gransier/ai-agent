package cn.gransier.common.aspect;

import cn.gransier.common.annotation.AgentService;
import cn.gransier.common.context.AgentContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class AgentAspect {

    @Around("@within(cn.gransier.common.annotation.AgentService)")
    public Object aroundAgentService(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Class<?> targetClass = method.getDeclaringClass();
            AgentService annotation = targetClass.getAnnotation(AgentService.class);
            if (annotation.value() != null) {
                AgentContext.setBaseUrl(annotation.value());
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String agentToken = request.getHeader("AgentToken");
            if (agentToken != null) {
                AgentContext.setApiKey(agentToken);
            }

            return joinPoint.proceed();
        } finally {
            AgentContext.clear();
        }
    }
}