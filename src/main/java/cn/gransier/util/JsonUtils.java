package cn.gransier.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;


@Component
public class JsonUtils implements ApplicationContextAware {

    @Getter
    private static ObjectMapper objectMapper;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        JsonUtils.objectMapper = context.getBean(ObjectMapper.class);

        // Configure the ObjectMapper to handle Java 8 date and time types.
        objectMapper.registerModule(new JavaTimeModule());
        // Disable the feature that fails on unknown properties when deserializing.
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Pretty print JSON output.
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 自动忽略 空值 字段
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public static String toJson(Object obj) {
        if (objectMapper == null) {
            // 防止在 Spring 完全启动前被静态调用
            throw new IllegalStateException("JsonUtils not initialized yet. Is Spring context ready?");
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseJson(String jsonStr, Class<T> clazz) {
        if (objectMapper == null) {
            // 防止在 Spring 完全启动前被静态调用
            throw new IllegalStateException("JsonUtils not initialized yet. Is Spring context ready?");
        }
        try {
            return objectMapper.readValue(jsonStr, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> parseMap(Object requestBody) {
        if (objectMapper == null) {
            // 防止在 Spring 完全启动前被静态调用
            throw new IllegalStateException("JsonUtils not initialized yet. Is Spring context ready?");
        }
        if (requestBody == null) {
            return Map.of();
        }
        Map<String, Object> map = objectMapper.convertValue(requestBody, new TypeReference<>() {
        });
        return map.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
