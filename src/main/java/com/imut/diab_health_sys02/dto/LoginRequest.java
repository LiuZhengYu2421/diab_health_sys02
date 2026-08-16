package com.imut.diab_health_sys02.dto;

import lombok.Data;

/**
 * 登录请求体
 */
@Data
public class LoginRequest {

    private String username;

    private String password;
}
