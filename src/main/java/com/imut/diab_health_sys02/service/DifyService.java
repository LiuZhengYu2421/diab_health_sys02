package com.imut.diab_health_sys02.service;

import com.imut.diab_health_sys02.dto.AssistantChatRequest;
import com.imut.diab_health_sys02.dto.AssistantChatVO;
import com.imut.diab_health_sys02.dto.DoctorChatRequest;
import com.imut.diab_health_sys02.dto.RiskPredictRequest;
import com.imut.diab_health_sys02.dto.RiskPredictVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Dify AI 服务
 */
public interface DifyService {

    /**
     * 糖尿病风险预测（方案 B：后端代理 Dify 工作流）
     * 1) disease = "是"（已确诊）：后端兜底，直接返回 diabetesType 对应类型的管理建议；
     * 2) disease = "否"（未确诊）：调用 Dify 工作流获取 AI 建议，
     *    并按标准风险评分表计算 riskScore 与 detail.items（工作流不输出评分明细，由后端补齐）。
     *
     * @param request 风险预测请求
     * @return 风险预测结果
     */
    RiskPredictVO predictRisk(RiskPredictRequest request);

    /**
     * 智能助手对话（方案：后端代理 Dify chat 应用）
     * 后端按 userId 自动从 user_risk_info 表读取健康档案，
     * 组装为 Dify chat 应用的表单变量（userId/sex/age/height/weight/familyHistory/
     * waistline/systolicPressure/isPregnancy/disease），前端无需手动输入。
     *
     * @param request 对话请求（userId + messages）
     * @return AI 回答与会话 ID
     */
    AssistantChatVO assistantChat(AssistantChatRequest request);

    /**
     * 智能助手对话（SSE 流式输出）
     * Agent Chat App 不支持 blocking 模式，后端按 streaming 方式调用 Dify，
     * 并将 Dify 返回的 SSE 事件流逐步透传给前端，实现打字机效果。
     *
     * @param request 对话请求（userId + messages）
     * @return SSE 事件流（text/event-stream）
     */
    SseEmitter assistantChatStream(AssistantChatRequest request);

    /**
     * 医师咨询（SSE 流式输出）
     * 按 doctorName 从 doctor_information 表查询该医生的 chat_token，
     * 以该凭据调用 Dify 医师咨询助手（chat-messages, streaming），
     * inputs 在健康档案基础上追加 department / doctor_name 角色扮演变量（对应医师咨询助手 yml）。
     *
     * @param request 医师咨询请求（userId + doctorName + messages）
     * @return SSE 事件流（text/event-stream）
     */
    SseEmitter doctorChatStream(DoctorChatRequest request);
}
