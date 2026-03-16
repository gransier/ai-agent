package cn.gransier.aspect;

import cn.gransier.annotation.AgentService;
import cn.gransier.context.AgentContext;
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

    @Around("@within(cn.gransier.annotation.AgentService)")
    public Object setAndClearApiKey(ProceedingJoinPoint joinPoint) throws Throwable {
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
            String difyToken = request.getHeader("DifyToken");
            if (difyToken != null) {
                AgentContext.setApiKey(difyToken);
            }

            return joinPoint.proceed();
        } finally {
            AgentContext.clear();
        }
    }
}