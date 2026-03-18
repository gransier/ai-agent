package cn.gransier.common.tool.registry;

import cn.gransier.common.tool.annotation.Tool;
import cn.gransier.common.tool.annotation.ToolService;
import cn.gransier.common.tool.domain.BaseTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ToolRegistry {

    private final Map<String, BaseTool> tools = new ConcurrentHashMap<>();
    private final ApplicationContext applicationContext;

    public ToolRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        Map<String, Object> toolBeans = applicationContext.getBeansWithAnnotation(ToolService.class);
        for (Object bean : toolBeans.values()) {
            if (bean instanceof BaseTool tool) {
                Tool annotation = AnnotationUtils.findAnnotation(bean.getClass(), Tool.class);
                if (annotation != null) {
                    String name = annotation.name().isEmpty() ? tool.getName() : annotation.name();
                    toolRegistry(name, tool);
                }
            }
        }
        log.info("ToolRegistry initialized with {} tools: {}", tools.size(), tools.keySet());
    }

    public void register(BaseTool tool) {
        toolRegistry(tool.getName(), tool);
    }

    public void register(String name, BaseTool tool) {
        toolRegistry(name, tool);
    }

    private void toolRegistry(String name, BaseTool tool) {
        if (tools.containsKey(name)) {
            log.warn("Tool '{}' already registered, skipping...", name);
            return;
        }
        tools.put(name, tool);
        log.debug("Registered tool: {}", name);
    }

    public void unregister(String name) {
        tools.remove(name);
        log.debug("Unregistered tool: {}", name);
    }

    public Optional<BaseTool> get(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    public List<BaseTool> getAll() {
        return new ArrayList<>(tools.values());
    }

    public Set<String> getToolNames() {
        return tools.keySet();
    }

    public List<Map<String, Object>> toFunctionSchemas() {
        return tools.values().stream()
                .map(this::toFunctionSchema)
                .collect(Collectors.toList());
    }

    public Map<String, Object> toFunctionSchema(BaseTool tool) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("name", tool.getName());
        schema.put("description", tool.getDescription());
        
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", buildProperties(tool.getParameters()));
        parameters.put("required", buildRequired(tool.getParameters()));
        schema.put("parameters", parameters);
        
        return schema;
    }

    private Map<String, Object> buildProperties(Map<String, BaseTool.ToolParamDefinition> paramMap) {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (Map.Entry<String, BaseTool.ToolParamDefinition> entry : paramMap.entrySet()) {
            BaseTool.ToolParamDefinition param = entry.getValue();
            Map<String, Object> prop = new LinkedHashMap<>();
            prop.put("type", param.getType());
            prop.put("description", param.getDescription());
            if (param.getEnums() != null && !param.getEnums().isEmpty()) {
                prop.put("enum", param.getEnums());
            }
            if (param.getDefaultValue() != null) {
                prop.put("default", param.getDefaultValue());
            }
            properties.put(entry.getKey(), prop);
        }
        return properties;
    }

    private List<String> buildRequired(Map<String, BaseTool.ToolParamDefinition> paramMap) {
        return paramMap.entrySet().stream()
                .filter(e -> e.getValue().isRequired())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return tools.isEmpty();
    }

    public int size() {
        return tools.size();
    }
}
