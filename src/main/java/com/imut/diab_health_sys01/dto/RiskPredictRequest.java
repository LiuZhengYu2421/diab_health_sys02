package com.imut.diab_health_sys01.dto;

import lombok.Data;

/**
 * 糖尿病风险预测请求体
 * 对应前端 src/api/dify.js riskPredict(data)
 */
@Data
public class RiskPredictRequest {

    /** 用户 ID */
    private Integer userId;

    /** 年龄（岁） */
    private Integer age;

    /** 性别：男 / 女 */
    private String sex;

    /** 身高（cm） */
    private Double height;

    /** 体重（kg） */
    private Double weight;

    /** 糖尿病家族史：是 / 否 */
    private String familyHistory;

    /** 腰围（cm，选填，未填时后端推断） */
    private Double waistline;

    /** 收缩压（mmHg，选填，未填时后端推断） */
    private Double systolicPressure;

    /** 是否怀孕：是 / 否 */
    private String isPregnancy;

    /** 是否已确诊糖尿病：是 / 否 */
    private String disease;

    /** 糖尿病类型（仅 disease 为 "是" 时必填）：1型糖尿病 / 2型糖尿病 / 妊娠糖尿病 / 其他类型 */
    private String diabetesType;
}
