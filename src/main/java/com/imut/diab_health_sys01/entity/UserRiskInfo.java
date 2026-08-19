package com.imut.diab_health_sys01.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户风险信息，对应表 user_risk_info
 * 注意：该表主键列名与其它表不同，为驼峰 userId（非 user_id）
 */
@Data
public class UserRiskInfo {

    /** 用户 id（主键） */
    private Integer userId;

    /** 年龄 */
    private Integer age;

    /** 性别：男 / 女 */
    private String sex;

    /** 身高（cm） */
    private Double height;

    /** 体重（kg） */
    private Double weight;

    /** 家族病史（JSON 数组字符串） */
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

    /** 档案更新时间 */
    private LocalDateTime updatedAt;
}
