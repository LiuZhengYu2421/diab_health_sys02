package com.imut.diab_health_sys02.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Dify AI 配置
 * 对应 application.properties 中 dify.* 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "dify")
public class DifyProperties {

    /** Dify 服务基础地址（文档约定调用 POST {base-url}/workflows/run） */
    private String baseUrl = "https://api.dify.ai/v1";

    /** 糖尿病风险预测工作流 API Key（仅用于后端调用 workflows/run） */
    private String riskKey = "";

    /** 智能助手（糖尿病专家，chat 模式）API Key（用于后端调用 chat-messages） */
    private String assistantKey = "";

    /** 调用超时时间（毫秒） */
    private int timeoutMs = 60000;
}
