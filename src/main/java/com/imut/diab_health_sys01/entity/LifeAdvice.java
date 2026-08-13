package com.imut.diab_health_sys01.entity;

import lombok.Data;

/**
 * 生活建议，对应表 life_advice
 */
@Data
public class LifeAdvice {

    /** 建议 id */
    private Integer id;

    /** 用户 id */
    private Integer userId;

    /** 标题 */
    private String title;

    /** 标签（逗号分隔） */
    private String tags;

    /** 内容 */
    private String content;
}
