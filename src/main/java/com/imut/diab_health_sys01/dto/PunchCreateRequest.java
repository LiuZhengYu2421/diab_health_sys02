package com.imut.diab_health_sys01.dto;

import lombok.Data;

/**
 * 新增打卡请求体（接口 9.1）
 * punch_time 由服务端生成；user_id 从 token 解析
 */
@Data
public class PunchCreateRequest {

    /** 打卡类型：血糖监测 / 饮食 / 运动 / 作息 */
    private String punchType;

    /** 完成状态：已完成 / 未完成 */
    private String completionStatus;

    /** 打卡备注（选填，最长 50 字） */
    private String message;
}
