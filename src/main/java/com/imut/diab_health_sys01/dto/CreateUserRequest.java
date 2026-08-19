package com.imut.diab_health_sys01.dto;

import lombok.Data;

/**
 * 管理端添加用户请求体
 */
@Data
public class CreateUserRequest {

    /** 用户名（3~20 位，唯一） */
    private String username;

    /** 密码（6~32 位） */
    private String password;

    /** 昵称（可选，默认取用户名） */
    private String nickname;

    /** 角色（可选，默认 user；可指定 user / doctor / admin） */
    private String role;
}
