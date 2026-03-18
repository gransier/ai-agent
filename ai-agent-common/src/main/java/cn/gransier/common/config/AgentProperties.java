package cn.gransier.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "agent")
public class AgentProperties {

    /**
     * 基础 URL
     */
    private String baseUrl;

    /**
     * 连接超时时间 (秒)
     */
    private long connectTimeout;

    /**
     * 读取超时时间 (秒)
     */
    private long readTimeout;

    /**
     * 写入超时时间 (秒)
     */
    private long writeTimeout;

    /**
     * 调用总超时时间 (秒)
     */
    private long callTimeout;

    /**
     * 连接失败是否重试
     */
    private boolean retryOnConnectionFailureTimeout;

}
