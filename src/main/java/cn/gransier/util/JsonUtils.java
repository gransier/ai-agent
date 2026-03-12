package cn.gransier.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;

public class JsonUtils {

    @Getter
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Configure the ObjectMapper to handle Java 8 date and time types.
        objectMapper.registerModule(new JavaTimeModule());
        // Disable the feature that fails on unknown properties when deserializing.
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Pretty print JSON output.
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 自动忽略 null 字段
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseJson(String jsonStr, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonStr, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> parseMap(Object requestBody) {
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
