package cn.gransier.common.config;

import cn.gransier.common.annotation.AgentMethod;
import cn.gransier.common.annotation.AgentParam;
import cn.gransier.common.config.listener.FluxStreamListener;
import cn.gransier.common.consts.AgentConst;
import cn.gransier.common.context.AgentContext;
import cn.gransier.common.util.TypeUtils;
import cn.gransier.common.util.AgentClient;
import cn.gransier.common.util.UploadUtils;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import reactor.core.publisher.Flux;

import java.lang.reflect.*;
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

    private AgentClient getDifyClient() {
        return beanFactory.getBean(AgentClient.class);
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
                case "equals" -> {
                    return proxy == args[0];
                }
                case "hashCode" -> {
                    return System.identityHashCode(proxy);
                }
                case "toString" -> {
                    return "AgentServiceProxy@" + Integer.toHexString(System.identityHashCode(proxy));
                }
                default -> {
                }
            }
        }

        AgentMethod annotation = method.getAnnotation(AgentMethod.class);
        if (annotation == null) {
            throw new RuntimeException("代理方法必须使用@AgentMethod注解");
        }
        if (AgentConst.MULTIPART_FORM_DATA.equals(annotation.contentType())) {
            return handleUpload(method, args, annotation);
        }
        if (method.getReturnType() == Flux.class) {
            return handleAgentFlux(method, args, annotation);
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
        AgentClient agentClient = getDifyClient();
        Object requestBody = buildRequestBody(method, args);
        String apiKey = AgentContext.getApiKey();
        String baseUrl = AgentContext.getBaseUrl();
        return Flux.create(sink -> agentClient.stream(
                annotation,
                apiKey,
                baseUrl,
                requestBody,
                FluxStreamListener.newInstance(sink, TypeUtils.getGenericReturnType(method, 0))
        ));
    }

    private Object handleHttp(Method method, Object[] args, AgentMethod annotation) {
        AgentClient agentClient = getDifyClient();
        Object requestBody = buildRequestBody(method, args);
        String apiKey = AgentContext.getApiKey();
        String baseUrl = AgentContext.getBaseUrl();

        return agentClient.http(annotation, apiKey, baseUrl, requestBody, method.getReturnType());
    }

    private Object handleUpload(Method method, Object[] args, AgentMethod annotation) {
        AgentClient agentClient = getDifyClient();
        String apiKey = AgentContext.getApiKey();
        String baseUrl = AgentContext.getBaseUrl();

        return agentClient.upload(annotation, apiKey, baseUrl, method.getReturnType(), UploadUtils.getMultipartBody(method, args));
    }
}
