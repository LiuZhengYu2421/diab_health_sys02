package com.imut.diab_health_sys01.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应表 users
 */
@Data
public class User {

    private Integer userId;

    /** 用户名（唯一） */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 个人简介（desc） */
    private String description;

    /** BCrypt 加密后的密码 */
    private String password;

    /** 头像地址 */
    private String avatarUrl;

    /** 角色：user / doctor / admin，注册默认 user */
    private String role;

    /** 注册时间 */
    private LocalDateTime createdAt;
}
