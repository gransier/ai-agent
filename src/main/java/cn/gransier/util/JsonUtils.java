package cn.gransier.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;

public class JsonUtils {

    @Getter
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Configure the ObjectMapper to handle Java 8 date and time types.
        objectMapper.registerModule(new JavaTimeModule());
        // Disable the feature that fails on unknown properties when deserializing.
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Pretty print JSON output.
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonStr, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
