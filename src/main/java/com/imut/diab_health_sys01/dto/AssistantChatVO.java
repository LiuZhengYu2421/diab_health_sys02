package com.imut.diab_health_sys01.dto;

import lombok.Data;

/**
 * 智能助手对话响应体
 * 对应前端 src/api/dify.js assistantChat 接口约定 { code:200, data: { answer, sessionId } }
 */
@Data
public class AssistantChatVO {

    /** AI 回答内容 */
    private String answer;

    /** 会话 ID（多轮对话时回传，用于保持上下文） */
    private String sessionId;
}
