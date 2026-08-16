package com.imut.diab_health_sys02.controller;

import com.imut.diab_health_sys02.common.Result;
import com.imut.diab_health_sys02.dto.ChangePasswordRequest;
import com.imut.diab_health_sys02.dto.UpdateUserRequest;
import com.imut.diab_health_sys02.dto.UserInfoVO;
import com.imut.diab_health_sys02.interceptor.AuthInterceptor;
import com.imut.diab_health_sys02.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口（需登录，token 由 AuthInterceptor 校验并注入 userId）
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 获取当前登录用户信息 */
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Integer userId) {
        return Result.success(userService.getUserInfo(userId));
    }

    /** 更新个人信息 */
    @PutMapping("/info")
    public Result<UserInfoVO> updateUserInfo(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Integer userId,
                                             @RequestBody UpdateUserRequest request) {
        return Result.success("更新成功", userService.updateUserInfo(userId, request));
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Integer userId,
                                       @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return Result.success("修改成功", null);
    }
}
