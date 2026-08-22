package com.imut.diab_health_sys02.dto;

import lombok.Data;

/**
 * 风险信息提交/更新请求体（userId 由服务端从 token 解析，前端不传）
 */
@Data
public class RiskInfoRequest {

    /** 年龄 */
    private Integer age;

    /** 性别：男 / 女 */
    private String sex;

    /** 身高（cm） */
    private Double height;

    /** 体重（kg） */
    private Double weight;

    /** 家族病史 */
    private String familyHistory;

    /** 腰围（cm） */
    private Double waistline;

    /** 收缩压（mmHg） */
    private Double systolicPressure;

    /** 是否妊娠：是 / 否 */
    private String isPregnancy;

    /** 风险提示 */
    private String message;

    /** 疑似疾病 */
    private String disease;

    /** 糖尿病类型：1型糖尿病 / 2型糖尿病 / 妊娠糖尿病 / 其他类型（仅 disease=是 时填写） */
    private String diabetesType;
}
