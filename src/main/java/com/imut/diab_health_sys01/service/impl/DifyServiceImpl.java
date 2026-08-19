package com.imut.diab_health_sys01.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imut.diab_health_sys01.common.BizException;
import com.imut.diab_health_sys01.config.DifyProperties;
import com.imut.diab_health_sys01.dto.AssistantChatRequest;
import com.imut.diab_health_sys01.dto.AssistantChatVO;
import com.imut.diab_health_sys01.dto.RiskPredictRequest;
import com.imut.diab_health_sys01.dto.RiskPredictVO;
import com.imut.diab_health_sys01.entity.UserRiskInfo;
import com.imut.diab_health_sys01.mapper.UserRiskInfoMapper;
import com.imut.diab_health_sys01.service.DifyService;
import com.imut.diab_health_sys01.util.DiabetesRiskUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dify 服务实现（糖尿病风险预测 - 方案 B：后端代理 Dify 工作流）
 *
 * 调用链路：
 *   前端 POST /api/dify/risk/predict
 *     → 后端转发 Dify 工作流 POST {base-url}/workflows/run（Authorization: Bearer {risk-key}）
 *     → 解析 data.outputs.obj = { result, disease }
 *     → disease="是"：后端兜底返回 diabetesType 对应类型固定管理建议；
 *       disease="否"：AI 建议取 result 文本，riskScore / detail.items 由后端按标准风险评分表补齐。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DifyServiceImpl implements DifyService {

    private final DifyProperties difyProperties;
    private final ObjectMapper objectMapper;
    private final UserRiskInfoMapper userRiskInfoMapper;

    /** 会话上下文：前端 sessionId -> Dify conversation_id（内存维护，重启后失效） */
    private final Map<String, String> sessionConvs = new ConcurrentHashMap<>();

    /** 各糖尿病类型对应的固定管理建议（disease=是 时兜底，与前端 mock 保持一致） */
    private static final Map<String, String> TYPE_ADVICE = new LinkedHashMap<>();

    static {
        TYPE_ADVICE.put("1型糖尿病", "1型糖尿病需长期胰岛素替代治疗，请遵医嘱规律用药，定期监测血糖与糖化血红蛋白，预防酮症酸中毒等急性并发症。");
        TYPE_ADVICE.put("2型糖尿病", "2型糖尿病以生活方式干预为基础，注意控制饮食、坚持运动、规律用药，定期复查血糖并筛查心、肾、眼底等并发症。");
        TYPE_ADVICE.put("妊娠糖尿病", "妊娠糖尿病需在产科与内分泌科共同指导下进行医学营养治疗与血糖监测，多数患者产后血糖可恢复正常，产后 4~12 周建议复查血糖。");
        TYPE_ADVICE.put("其他类型", "其他特殊类型糖尿病需针对病因治疗，请在专科医生指导下制定个体化降糖方案并规律复诊。");
    }

    @Override
    public RiskPredictVO predictRisk(RiskPredictRequest request) {
        if (request == null) {
            throw BizException.badRequest("请求参数不能为空");
        }
        if (!StringUtils.hasText(request.getDisease())) {
            throw BizException.badRequest("disease（是否已确诊糖尿病）不能为空");
        }

        log.info("[Dify] ====== 风险预测请求进入 ====== userId={}, disease={}, age={}, sex={}, height={}, weight={}, familyHistory={}, waistline={}, systolicPressure={}, isPregnancy={}, diabetesType={}",
                request.getUserId(), request.getDisease(), request.getAge(), request.getSex(), request.getHeight(),
                request.getWeight(), request.getFamilyHistory(), request.getWaistline(), request.getSystolicPressure(),
                request.getIsPregnancy(), request.getDiabetesType());

        if ("是".equals(request.getDisease())) {
            // 已确诊：后端兜底，不调 Dify 工作流
            log.info("[Dify] disease=是（已确诊），走后端兜底管理建议，不调用 Dify 工作流");
            return predictDiagnosed(request);
        }
        // 未确诊：调用 Dify 工作流 + 后端补齐评分明细
        log.info("[Dify] disease=否（未确诊），进入 Dify 工作流调用流程");
        return predictUndiagnosed(request);
    }

    /**
     * 已确诊（disease = "是"）：直接返回糖尿病类型对应固定管理建议，不计算风险评分
     */
    private RiskPredictVO predictDiagnosed(RiskPredictRequest request) {
        String type = request.getDiabetesType();
        if (!StringUtils.hasText(type)) {
            throw BizException.badRequest("已确诊时 diabetesType（糖尿病类型）不能为空");
        }
        String advice = TYPE_ADVICE.get(type);
        if (advice == null) {
            advice = "您已确诊" + type + "，请遵医嘱规律治疗，保持健康生活方式，并定期复查随访。";
        }

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("diabetesType", type);
        detail.put("age", request.getAge() != null ? String.valueOf(request.getAge()) : "未填写");
        detail.put("familyHistory", StringUtils.hasText(request.getFamilyHistory()) ? request.getFamilyHistory() : "未填写");

        RiskPredictVO vo = new RiskPredictVO();
        vo.setRiskLevel(type);
        vo.setRiskScore(0);
        vo.setAdvice(advice);
        vo.setDetail(detail);
        log.info("[Dify] 已确诊返回: riskLevel={}, advice={}", type, advice);
        return vo;
    }

    /**
     * 未确诊（disease = "否"）：
     *  1) 调用 Dify 工作流获取 AI 建议（result 文本）；
     *  2) riskScore / riskLevel / detail.items 按标准风险评分表由后端计算补齐。
     */
    private RiskPredictVO predictUndiagnosed(RiskPredictRequest request) {
        // 后端评分（与前端 src/utils/diabetesRisk.js 逻辑一致）
        DiabetesRiskUtil.RiskParams params = new DiabetesRiskUtil.RiskParams();
        params.age = request.getAge() != null ? request.getAge().doubleValue() : null;
        params.sex = request.getSex();
        params.height = request.getHeight();
        params.weight = request.getWeight();
        params.familyHistory = request.getFamilyHistory();
        params.waistline = request.getWaistline();
        params.systolicPressure = request.getSystolicPressure();
        DiabetesRiskUtil.RiskResult risk = DiabetesRiskUtil.calcDiabetesRisk(params);

        // 调用 Dify 工作流获取 AI 建议
        String aiResult = null;
        try {
            aiResult = callRiskWorkflow(request);
            log.info("[Dify] 工作流调用完成，AI 建议: {}", aiResult);
        } catch (Exception e) {
            log.warn("[Dify] 调用风险预测工作流失败: {}", e.getMessage(), e);
            throw new BizException(500, "AI 服务暂不可用，请稍后重试");
        }

        // AI 建议为空时使用后端模板兜底
        String advice = StringUtils.hasText(aiResult) ? aiResult.trim() : risk.advice;

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("total", risk.total);
        detail.put("items", risk.items);
        detail.put("bmi", risk.bmi != null ? String.format("%.1f", risk.bmi) : "未填写");
        detail.put("waistline", risk.waist != null ? numStr(risk.waist) + (risk.waistPredicted ? "（预测）" : "") : "未填写");
        detail.put("systolicPressure", risk.bp != null ? numStr(risk.bp) + (risk.bpPredicted ? "（预测）" : "") : "未填写");

        RiskPredictVO vo = new RiskPredictVO();
        vo.setRiskLevel(risk.level);
        vo.setRiskScore(risk.total);
        vo.setAdvice(advice);
        vo.setDetail(detail);
        log.info("[Dify] 未确诊预测返回: riskLevel={}, riskScore={}, advice={}", risk.level, risk.total, advice);
        return vo;
    }

    @Override
    public AssistantChatVO assistantChat(AssistantChatRequest request) {
        if (request == null || request.getUserId() == null) {
            throw BizException.badRequest("userId 不能为空");
        }
        List<Map<String, String>> messages = request.getMessages() == null ? new ArrayList<>() : request.getMessages();
        // 取最后一条用户消息作为本次提问
        String query = "";
        for (int i = messages.size() - 1; i >= 0; i--) {
            String role = messages.get(i).get("role");
            if ("user".equals(role) && StringUtils.hasText(messages.get(i).get("content"))) {
                query = messages.get(i).get("content").trim();
                break;
            }
        }
        if (!StringUtils.hasText(query)) {
            throw BizException.badRequest("请先输入问题内容");
        }

        // 会话：sessionId 为空则新建，否则复用 Dify conversation_id
        String sessionId = request.getSessionId();
        if (!StringUtils.hasText(sessionId)) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }

        // 关键：按 userId 从 user_risk_info 表自动读取健康档案，组装表单变量，用户无需手动输入
        UserRiskInfo info = userRiskInfoMapper.findByUserId(request.getUserId());
        log.info("[Dify] 智能助手：userId={}, 档案={}", request.getUserId(), info == null ? "（无档案）" : info);

        String answer;
        try {
            answer = callAssistantChat(request.getUserId(), query, sessionId, info);
            log.info("[Dify] 智能助手回答: {}", answer);
        } catch (Exception e) {
            log.warn("[Dify] 智能助手调用失败: {}", e.getMessage(), e);
            throw new BizException(500, "AI 服务暂不可用，请稍后重试");
        }

        AssistantChatVO vo = new AssistantChatVO();
        vo.setAnswer(answer);
        vo.setSessionId(sessionId);
        return vo;
    }

    /**
     * 调用 Dify chat 应用 chat-messages（blocking 模式）。
     * inputs 自动从 user_risk_info 档案组装，用户无需在界面手动填写表单变量。
     */
    private String callAssistantChat(Integer userId, String query, String sessionId, UserRiskInfo info) throws Exception {
        String url = difyProperties.getBaseUrl() + "/chat-messages";
        log.info("[Dify] 智能助手目标地址: POST {}", url);

        // 会话上下文：sessionId -> Dify conversation_id，实现多轮对话
        String conversationId = sessionConvs.getOrDefault(sessionId, "");

        // 组装 chat 应用表单变量（与「糖尿病专家」应用 user_input_form 一致，自动从档案填充；
        // 始终提供全部字段，无档案时填空字符串，避免 Dify 端必填校验失败）
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("userId", String.valueOf(userId));
        inputs.put("sex", info != null ? nvl(info.getSex()) : "");
        inputs.put("age", info != null && info.getAge() != null ? String.valueOf(info.getAge()) : "");
        inputs.put("height", info != null && info.getHeight() != null ? numStr(info.getHeight()) : "");
        inputs.put("weight", info != null && info.getWeight() != null ? numStr(info.getWeight()) : "");
        inputs.put("familyHistory", info != null ? nvl(info.getFamilyHistory()) : "");
        inputs.put("waistline", info != null && info.getWaistline() != null ? numStr(info.getWaistline()) : "");
        inputs.put("systolicPressure", info != null && info.getSystolicPressure() != null ? numStr(info.getSystolicPressure()) : "");
        inputs.put("isPregnancy", info != null ? nvl(info.getIsPregnancy()) : "");
        inputs.put("disease", info != null ? nvl(info.getDisease()) : "");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("inputs", inputs);
        body.put("query", query);
        body.put("response_mode", "blocking");
        body.put("conversation_id", conversationId);
        body.put("user", "user-" + userId);

        String jsonBody = objectMapper.writeValueAsString(body);
        log.info("[Dify] 智能助手请求体: {}", jsonBody);
        String resp = postJsonWithKey(url, jsonBody, difyProperties.getAssistantKey());
        log.info("[Dify] 智能助手原始响应: {}", resp);

        JsonNode root = objectMapper.readTree(resp);
        String answer = root.path("answer").asText("");
        String convId = root.path("conversation_id").asText("");
        if (StringUtils.hasText(convId)) {
            sessionConvs.put(sessionId, convId);
        }
        if (!StringUtils.hasText(answer)) {
            log.warn("[Dify] 智能助手 answer 为空: {}", resp);
        }
        return answer;
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    /**
     * 调用 Dify 工作流 workflows/run（blocking 模式），
     * 返回 data.outputs.obj.result 文本（AI 建议，格式形如：【高风险】"建议内容"）；
     * 工作流返回为空时返回 null。
     */
    private String callRiskWorkflow(RiskPredictRequest request) throws Exception {
        String url = difyProperties.getBaseUrl() + "/workflows/run";
        log.info("[Dify] 目标工作流地址: POST {}", url);

        // 透传 Dify 工作流起始变量（含 userId，工作流 execute_sql 节点依赖 userId 写库）
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("userId", request.getUserId());
        inputs.put("age", request.getAge());
        inputs.put("sex", request.getSex());
        inputs.put("height", request.getHeight());
        inputs.put("weight", request.getWeight());
        inputs.put("familyHistory", request.getFamilyHistory());
        inputs.put("waistline", request.getWaistline());
        inputs.put("systolicPressure", request.getSystolicPressure());
        inputs.put("isPregnancy", request.getIsPregnancy());
        inputs.put("disease", request.getDisease());
        inputs.put("diabetesType", request.getDiabetesType());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("inputs", inputs);
        body.put("response_mode", "blocking");
        body.put("user", "user-" + (request.getUserId() == null ? "anonymous" : request.getUserId()));

        String jsonBody = objectMapper.writeValueAsString(body);
        log.info("[Dify] 发送请求体: {}", jsonBody);
        String resp = postJson(url, jsonBody);
        log.info("[Dify] 工作流原始响应: {}", resp);

        JsonNode root = objectMapper.readTree(resp);
        JsonNode dataNode = root.path("data");
        String status = dataNode.path("status").asText("");
        log.info("[Dify] 工作流状态: status={}, error={}", status, dataNode.path("error").asText(""));
        if (!"succeeded".equals(status)) {
            log.warn("[Dify] 工作流未成功: status={}, error={}, 原始响应={}", status, dataNode.path("error").asText(""), resp);
            return null;
        }

        // Dify blocking 模式：data.outputs 为 JSON 对象 { obj: { result, disease } }，
        // 兼容个别版本 outputs 为字符串的情况
        JsonNode outputsNode = dataNode.path("outputs");
        String outputs = outputsNode.isTextual() ? outputsNode.asText("") : outputsNode.toString();
        if (outputs == null || outputs.isEmpty()) {
            log.warn("[Dify] 风险预测工作流 outputs 为空: {}", resp);
            return null;
        }
        JsonNode obj = objectMapper.readTree(outputs).path("obj");
        String result = obj.path("result").asText(null);
        log.info("[Dify] 解析工作流 result: {}", result);
        if (!StringUtils.hasText(result)) {
            log.warn("[Dify] 风险预测工作流 obj.result 为空");
            return null;
        }
        String advice = extractAdvice(result);
        log.info("[Dify] 提取后的建议文本: {}", advice);
        return advice;
    }

    /**
     * 从工作流 result 文本中提取建议内容。
     * 工作流 LLM 输出格式：【低风险/高风险】"建议内容" 或 【2 型糖尿病】"建议内容"，
     * 提取最外层引号（中文引号「」或英文引号 "）内的内容；提取不到则返回原文。
     */
    private String extractAdvice(String text) {
        String s = text.trim();
        if (s.isEmpty()) {
            return "";
        }
        // 去除开头的【等级】前缀
        s = s.replaceFirst("^【[^】]*】", "").trim();
        // 提取引号内内容（支持中文引号「」与英文引号 ""）
        int quoteStart = -1;
        char close = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '「') {
                quoteStart = i;
                close = '」';
                break;
            }
            if (c == '"') {
                quoteStart = i;
                close = '"';
                break;
            }
            if (c == '“') {
                quoteStart = i;
                close = '”';
                break;
            }
        }
        if (quoteStart >= 0) {
            int end = s.indexOf(close, quoteStart + 1);
            if (end > quoteStart) {
                return s.substring(quoteStart + 1, end).trim();
            }
        }
        return s;
    }

    /** POST JSON 请求（Authorization: Bearer {risk-key}） */
    private String postJson(String url, String jsonBody) throws IOException {
        return postJsonWithKey(url, jsonBody, difyProperties.getRiskKey());
    }

    /** POST JSON 请求，可指定 API Key（risk-key / assistant-key） */
    private String postJsonWithKey(String url, String jsonBody, String apiKey) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(difyProperties.getTimeoutMs());
            conn.setReadTimeout(difyProperties.getTimeoutMs());
            log.info("[Dify] 发起 HTTP 连接: {} (timeout={}ms)", url, difyProperties.getTimeoutMs());
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Authorization", "Bearer " + (apiKey == null ? "" : apiKey));
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            log.info("[Dify] 工作流 HTTP 状态码: {}", code);
            InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String text = readAll(is);
            if (code >= 400) {
                throw new IOException("Dify HTTP " + code + ": " + text);
            }
            return text;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String readAll(InputStream is) throws IOException {
        if (is == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /** 数字展示：整数去掉小数位，其余保留原样 */
    private String numStr(Double v) {
        if (v == null) {
            return "";
        }
        long l = Math.round(v);
        if (Math.abs(v - l) < 0.000001) {
            return String.valueOf(l);
        }
        return String.valueOf(v);
    }
}
