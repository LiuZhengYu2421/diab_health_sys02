package com.imut.diab_health_sys02.service;


import com.imut.diab_health_sys02.dto.*;

/**
 * 用户认证与信息管理服务
 */
public interface UserService {

    /** 登录 */
    LoginResult login(LoginRequest request);

    /** 注册 */
    LoginResult register(RegisterRequest request);

    /** 获取当前用户信息 */
    UserInfoVO getUserInfo(Integer userId);

    /** 更新个人信息 */
    UserInfoVO updateUserInfo(Integer userId, UpdateUserRequest request);

    /** 修改密码 */
    void changePassword(Integer userId, ChangePasswordRequest request);
}
