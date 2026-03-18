package cn.gransier.common.tool.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface BaseTool extends Serializable {

    String getName();

    String getDescription();

    Map<String, ToolParamDefinition> getParameters();

    Object invoke(Map<String, Object> params);

    default boolean isAsync() {
        return true;
    }

    @Data
    @Builder
    class ToolParamDefinition implements Serializable {
        private String name;
        private String type;
        private String description;
        private boolean required;
        private Object defaultValue;
        private List<String> enums;
    }
}
