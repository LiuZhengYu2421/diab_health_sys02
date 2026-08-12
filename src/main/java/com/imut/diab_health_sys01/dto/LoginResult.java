package com.imut.diab_health_sys01.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录/注册成功返回：{ token, userInfo }
 */
@Data
@AllArgsConstructor
public class LoginResult {

    private String token;

    private UserInfoVO userInfo;
}
