package cn.gransier.tools.examples;

import cn.gransier.common.tool.annotation.Tool;
import cn.gransier.common.tool.annotation.ToolService;
import cn.gransier.common.tool.domain.BaseTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TimeZone;

@Slf4j
@Component
@ToolService
public class TimeTool implements BaseTool {

    @Override
    public String getName() {
        return "get_time";
    }

    @Override
    public String getDescription() {
        return "获取当前日期和时间信息";
    }

    @Override
    public Map<String, ToolParamDefinition> getParameters() {
        return Map.of(
                "timezone", ToolParamDefinition.builder()
                        .name("timezone")
                        .type("string")
                        .description("时区，如 Asia/Shanghai, America/New_York")
                        .required(false)
                        .defaultValue("Asia/Shanghai")
                        .build(),
                "format", ToolParamDefinition.builder()
                        .name("format")
                        .type("string")
                        .description("日期格式，full(完整) 或 date(仅日期) 或 time(仅时间)")
                        .required(false)
                        .defaultValue("full")
                        .enums(java.util.List.of("full", "date", "time"))
                        .build()
        );
    }

    @Override
    public Object invoke(Map<String, Object> params) {
        String timezone = (String) params.getOrDefault("timezone", "Asia/Shanghai");
        String format = (String) params.getOrDefault("format", "full");
        
        log.info("Getting time for timezone: {}, format: {}", timezone, format);
        
        try {
            var now = java.time.ZonedDateTime.now(TimeZone.getTimeZone(timezone).toZoneId());
            
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("timezone", timezone);
            result.put("timestamp", now.toEpochSecond());
            
            switch (format) {
                case "date" -> {
                    result.put("date", now.toLocalDate().toString());
                    return result;
                }
                case "time" -> {
                    result.put("time", now.toLocalTime().toString().substring(0, 8));
                    return result;
                }
                default -> {
                    result.put("datetime", now.toString());
                    result.put("date", now.toLocalDate().toString());
                    result.put("time", now.toLocalTime().toString().substring(0, 8));
                    result.put("weekday", now.getDayOfWeek().toString());
                    return result;
                }
            }
        } catch (Exception e) {
            return Map.of(
                    "error", "Invalid timezone: " + timezone,
                    "available_timezones", java.util.List.of("Asia/Shanghai", "America/New_York", "Europe/London", "Asia/Tokyo")
            );
        }
    }
}
