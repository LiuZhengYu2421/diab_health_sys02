package com.imut.diab_health_sys02.entity;

import lombok.Data;

/**
 * 生活方案，对应表 life_plans
 * 注意：order 为 MySQL 保留字，MyBatis SQL 中需用反引号 `order`
 */
@Data
public class LifePlan {

    /** 方案 id */
    private Integer id;

    /** 用户 id */
    private Integer userId;

    /** 方案类型：饮食 / 运动 / 作息 */
    private String type;

    /** 排序 */
    private Integer order;

    /** 时间段描述（如 早餐后） */
    private String time;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;
}
