package cn.gransier.common.tool.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionResult implements Serializable {
    private String toolCallId;
    private String toolName;
    private Object result;
    private String error;
    private long duration;
    private LocalDateTime timestamp;

    public static FunctionResult success(String toolCallId, String toolName, Object result, long duration) {
        return FunctionResult.builder()
                .toolCallId(toolCallId)
                .toolName(toolName)
                .result(result)
                .duration(duration)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static FunctionResult error(String toolCallId, String toolName, String error) {
        return FunctionResult.builder()
                .toolCallId(toolCallId)
                .toolName(toolName)
                .error(error)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public boolean isSuccess() {
        return error == null;
    }
}
