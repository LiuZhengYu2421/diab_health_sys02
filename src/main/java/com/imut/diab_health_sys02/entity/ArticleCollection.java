package com.imut.diab_health_sys02.entity;

import lombok.Data;

/**
 * 文章收藏，对应表 article_collections
 */
@Data
public class ArticleCollection {

    /** 收藏记录 id */
    private Integer collectionId;

    /** 用户 id */
    private Integer userId;

    /** 文章 id */
    private Integer articleId;
}
