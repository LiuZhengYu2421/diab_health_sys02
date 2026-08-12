package com.imut.diab_health_sys01.dto;

import lombok.Data;

/**
 * 更新个人信息请求体（只需传要修改的字段）
 */
@Data
public class UpdateUserRequest {

    private String nickname;

    private String avatar;

    private String desc;
}
