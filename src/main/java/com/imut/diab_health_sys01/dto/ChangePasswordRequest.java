package com.imut.diab_health_sys01.dto;

import lombok.Data;

/**
 * 修改密码请求体
 */
@Data
public class ChangePasswordRequest {

    private String oldPassword;

    private String newPassword;
}
