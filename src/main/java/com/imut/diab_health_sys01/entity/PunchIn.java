package com.imut.diab_health_sys01.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 打卡记录，对应表 punch_in
 */
@Data
public class PunchIn {

    /** 打卡记录 id */
    private Integer id;

    /** 用户 id */
    private Integer userId;

    /** 打卡时间 */
    private LocalDateTime punchTime;

    /** 打卡类型：饮食 / 运动 / 作息 */
    private String punchType;

    /** 完成状态：完成 / 未完成 */
    private String completionStatus;

    /** 打卡备注 */
    private String message;
}
