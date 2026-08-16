package com.imut.diab_health_sys02.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 科普文章，对应表 articles
 */
@Data
public class Article {

    /** 文章 id */
    private Integer articleId;

    /** 文章标题 */
    private String title;

    /** 封面图 */
    private String coverUrl;

    /** 作者 */
    private String author;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 文章内容 */
    private String content;

    /** 分类 */
    private String category;

    /** 浏览量 */
    private Integer views;
}
