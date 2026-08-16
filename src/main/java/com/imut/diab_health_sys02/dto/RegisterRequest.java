package com.imut.diab_health_sys02.dto;

import lombok.Data;

/**
 * 注册请求体
 */
@Data
public class RegisterRequest {

    /** 3~20 位，唯一 */
    private String username;

    /** 至少 6 位 */
    private String password;

    /** 可选，默认取 username */
    private String nickname;
}
