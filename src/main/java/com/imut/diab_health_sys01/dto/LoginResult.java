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
//{
//        "code": 200,
//        "message": "登录成功",
//        "data": {
//        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoidXNlciIsImV4cCI6MTc4NzE5NDkwMywidXNlcklkIjo0LCJ1c2VybmFtZSI6Imh6cCJ9.T4bJ73eOfXWTFEcdOOFe5ea5TeVTYY7lDu1dEWaIAB0",
//        "userInfo": {
//        "id": 4,
//        "username": "hzp",
//        "nickname": "hzp",
//        "avatar": "/img/user_icon.png",
//        "desc": null,
//        "role": "user",
//        "createdAt": "2026-08-12"
//        }
//        }
//        }