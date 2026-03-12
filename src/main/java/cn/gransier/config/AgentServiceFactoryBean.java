package cn.gransier.config;

import cn.gransier.annotation.AgentMethod;
import cn.gransier.annotation.AgentParam;
import cn.gransier.listener.FluxDifyStreamListener;
import cn.gransier.util.DifyClient;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import reactor.core.publisher.Flux;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class AgentServiceFactoryBean implements FactoryBean<Object>, InvocationHandler, BeanFactoryAware {

    private Class<?> mapperClass;
    private BeanFactory beanFactory;

    @SuppressWarnings("unused")
    public void setMapperClass(Class<?> mapperClass) {
        this.mapperClass = mapperClass;
    }

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private DifyClient getDifyClient() {
        return beanFactory.getBean(DifyClient.class);
    }

    @Override
    public Object getObject() {
        return Proxy.newProxyInstance(
                mapperClass.getClassLoader(),
                new Class[]{mapperClass},
                this
        );
    }

    @Override
    public Class<?> getObjectType() {
        return mapperClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Class<?> declaringClass = method.getDeclaringClass();
        if (Object.class.equals(declaringClass)) {
            switch (method.getName()) {
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "AgentServiceProxy@" + Integer.toHexString(System.identityHashCode(proxy));
                default:
                    break;
            }
        }

        AgentMethod annotation = method.getAnnotation(AgentMethod.class);
        if (method.getReturnType() == Flux.class) {
            if (annotation != null) {
                return handleAgentFlux(method, args, annotation);
            }
            throw new RuntimeException("代理方法必须使用@AgentMethod注解");
        }
        return handleHttp(method, args, annotation);
    }

    private Object buildRequestBody(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();

        if (parameters.length == 1) {
            Object arg = args[0];
            if (arg != null && !isPrimitive(arg.getClass())) {
                return arg;
            }
        }

        Map<String, Object> body = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            AgentParam paramAnnotation = parameters[i].getAnnotation(AgentParam.class);
            String paramName = paramAnnotation != null ? paramAnnotation.value() : parameters[i].getName();
            body.put(paramName.trim(), args[i]);
        }
        return body;
    }

    private boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() || clazz == String.class || clazz == Integer.class ||
                clazz == Long.class || clazz == Boolean.class || clazz == Double.class;
    }

    @SuppressWarnings("all")
    private Object handleAgentFlux(Method method, Object[] args, AgentMethod annotation) {
        DifyClient difyClient = getDifyClient();
        Object requestBody = buildRequestBody(method, args);

        return Flux.<String>create(sink -> difyClient.stream(
                annotation,
                requestBody,
                FluxDifyStreamListener.newInstance(sink)
        ));
    }

    private Object handleHttp(Method method, Object[] args, AgentMethod annotation) {
        DifyClient difyClient = getDifyClient();
        Object requestBody = buildRequestBody(method, args);
        return difyClient.http(annotation, requestBody, method.getReturnType());
    }
}
