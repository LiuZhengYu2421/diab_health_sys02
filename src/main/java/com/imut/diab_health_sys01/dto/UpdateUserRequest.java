package com.imut.diab_health_sys01.dto;

import lombok.Data;

import java.util.Map;

/**
 * 更新个人信息请求体（只需传要修改的字段）
 */
@Data
public class UpdateUserRequest {

    private String nickname;

    private String avatar;

    private String desc;

    /**
     * 糖尿病预测健康档案（个人中心「糖尿病预测信息」保存时传入）。
     * 字段：disease / diabetesType / sex / age / height / weight / familyHistory / waistline / systolicPressure / isPregnancy
     * 存于 user_risk_info 表，与 Dify 工作流共用同一张表。
     */
    private Map<String, Object> healthInfo;
}
