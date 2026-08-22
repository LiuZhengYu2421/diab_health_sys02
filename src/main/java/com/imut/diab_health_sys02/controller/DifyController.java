package com.imut.diab_health_sys02.controller;

import com.imut.diab_health_sys02.common.Result;
import com.imut.diab_health_sys02.dto.AssistantChatRequest;
import com.imut.diab_health_sys02.dto.AssistantChatVO;
import com.imut.diab_health_sys02.dto.DoctorChatRequest;
import com.imut.diab_health_sys02.dto.RiskPredictRequest;
import com.imut.diab_health_sys02.dto.RiskPredictVO;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.imut.diab_health_sys02.service.DifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dify AI 接口（前端 request.js baseURL=/api，context-path=/api，故完整路径为 /api/dify/risk/predict）
 */
@RestController
@RequestMapping("/dify")
@RequiredArgsConstructor
public class DifyController {

    private final DifyService difyService;

    /**
     * 糖尿病风险预测
     * 前端：src/api/dify.js riskPredict(data)
     * 请求：{ userId, age, sex, height, weight, familyHistory, waistline, systolicPressure, isPregnancy, disease, diabetesType }
     * 响应：{ code:200, data: { riskLevel, riskScore, advice, detail } }
     */
    @PostMapping("/risk/predict")
    public Result<RiskPredictVO> riskPredict(@RequestBody RiskPredictRequest request) {
        return Result.success(difyService.predictRisk(request));
    }

    /**
     * 智能助手对话（糖尿病专家）
     * 前端：src/api/dify.js assistantChat(data)
     * 请求：{ userId, sessionId, messages: [{ role, content }] }
     * 说明：后端按 userId 自动从 user_risk_info 表读取健康档案填充表单变量，前端无需手动输入
     * 响应：{ code:200, data: { answer, sessionId } }
     */
    @PostMapping("/assistant/chat")
    public Result<AssistantChatVO> assistantChat(@RequestBody AssistantChatRequest request) {
        return Result.success(difyService.assistantChat(request));
    }

    /**
     * 智能助手对话（SSE 流式输出，打字机效果）
     * 前端：src/api/dify.js assistantChatStream(data, handlers)
     * 请求：{ userId, sessionId, messages: [{ role, content }] }
     * 说明：后端按 streaming 调用 Dify chat-messages，并将 SSE 事件流原样透传给前端，
     *       前端逐块渲染实现 AI 打字机效果。事件格式见 DifyServiceImpl#forwardAssistantChatStream。
     */
    @PostMapping(value = "/assistant/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter assistantChatStream(@RequestBody AssistantChatRequest request) {
        return difyService.assistantChatStream(request);
    }

    /**
     * 医师咨询（SSE 流式输出，打字机效果）
     * 前端：src/api/dify.js doctorChatStream(data, handlers)
     * 请求：{ userId, sessionId, doctorName, department, messages: [{ role, content }] }
     * 说明：后端按 doctorName 查 doctor_information.chat_token 调 Dify 医师咨询助手（streaming），
     *       inputs 在健康档案基础上追加 department / doctor_name（对应「医师咨询助手」yml），
     *       并把 SSE 事件流原样透传给前端。事件格式见 DifyServiceImpl#forwardDoctorChatStream。
     */
    @PostMapping(value = "/doctor/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doctorChatStream(@RequestBody DoctorChatRequest request) {
        return difyService.doctorChatStream(request);
    }
}
