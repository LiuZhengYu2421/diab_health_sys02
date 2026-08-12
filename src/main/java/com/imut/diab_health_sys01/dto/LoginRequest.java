package com.imut.diab_health_sys01.dto;

import lombok.Data;

/**
 * 登录请求体
 */
@Data
public class LoginRequest {

    private String username;

    private String password;
}
