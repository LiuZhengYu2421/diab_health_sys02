package com.imut.diab_health_sys01.dto;

import lombok.Data;

import java.util.Map;

/**
 * 糖尿病风险预测响应体
 * 对应前端 RiskPredictView.vue 中 data.riskLevel / data.riskScore / data.advice / data.detail
 */
@Data
public class RiskPredictVO {

    /** 风险等级：低风险 / 中风险 / 高风险；已确诊时为糖尿病类型 */
    private String riskLevel;

    /** 风险评分（0 - 51 分）；已确诊时为 0 */
    private Integer riskScore;

    /** 建议文本（AI 建议或后端模板兜底） */
    private String advice;

    /**
     * 明细：
     *  - 未患病：{ total, bmi, waistline, systolicPressure, items:[{key,label,value,score}] }
     *  - 已确诊：{ diabetesType, age, familyHistory }
     */
    private Map<String, Object> detail;
}
