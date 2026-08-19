package com.imut.diab_health_sys01.controller;

import com.imut.diab_health_sys01.common.Result;
import com.imut.diab_health_sys01.dto.AssistantChatRequest;
import com.imut.diab_health_sys01.dto.AssistantChatVO;
import com.imut.diab_health_sys01.dto.RiskPredictRequest;
import com.imut.diab_health_sys01.dto.RiskPredictVO;
import com.imut.diab_health_sys01.service.DifyService;
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
}
