package com.imut.diab_health_sys01.controller;

import com.imut.diab_health_sys01.common.Result;
import com.imut.diab_health_sys01.dto.LoginRequest;
import com.imut.diab_health_sys01.dto.LoginResult;
import com.imut.diab_health_sys01.dto.RegisterRequest;
import com.imut.diab_health_sys01.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录 / 注册 / 退出
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /** 登录 */
    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody LoginRequest request) {
        return Result.success("登录成功", userService.login(request));
    }

    /** 注册 */
    @PostMapping("/register")
    public Result<LoginResult> register(@RequestBody RegisterRequest request) {
        return Result.success("注册成功", userService.register(request));
    }

    /** 退出登录（前端清除本地 token 即可） */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success(null);
    }
}
