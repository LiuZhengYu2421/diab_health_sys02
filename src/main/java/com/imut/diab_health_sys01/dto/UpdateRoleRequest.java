package com.imut.diab_health_sys01.dto;

import lombok.Data;

/**
 * 管理端修改用户角色请求体
 */
@Data
public class UpdateRoleRequest {

    /** 目标角色：user / doctor / admin */
    private String role;
}
