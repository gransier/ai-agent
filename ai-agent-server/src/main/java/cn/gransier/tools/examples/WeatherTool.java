package cn.gransier.tools.examples;

import cn.gransier.common.tool.annotation.Tool;
import cn.gransier.common.tool.annotation.ToolService;
import cn.gransier.common.tool.domain.BaseTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@ToolService
public class WeatherTool implements BaseTool {

    @Override
    public String getName() {
        return "get_weather";
    }

    @Override
    public String getDescription() {
        return "获取指定城市的当前天气信息，包括温度、湿度、风力等";
    }

    @Override
    public Map<String, ToolParamDefinition> getParameters() {
        return Map.of(
                "city", ToolParamDefinition.builder()
                        .name("city")
                        .type("string")
                        .description("城市名称，需要查询天气的城市")
                        .required(true)
                        .build(),
                "unit", ToolParamDefinition.builder()
                        .name("unit")
                        .type("string")
                        .description("温度单位，celsius(摄氏度) 或 fahrenheit(华氏度)")
                        .required(false)
                        .defaultValue("celsius")
                        .enums(java.util.List.of("celsius", "fahrenheit"))
                        .build()
        );
    }

    @Override
    public Object invoke(Map<String, Object> params) {
        String city = (String) params.get("city");
        String unit = (String) params.getOrDefault("unit", "celsius");
        
        log.info("Querying weather for city: {}, unit: {}", city, unit);
        
        Map<String, Object> weatherData = Map.of(
                "city", city,
                "temperature", unit.equals("celsius") ? 22 : 72,
                "unit", unit,
                "humidity", 65,
                "wind_speed", "3级",
                "weather", "多云转晴",
                "update_time", java.time.LocalDateTime.now().toString()
        );
        
        return weatherData;
    }
}
