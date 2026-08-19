package com.imut.diab_health_sys01.service;

import com.imut.diab_health_sys01.common.PageResult;
import com.imut.diab_health_sys01.dto.CreateUserRequest;
import com.imut.diab_health_sys01.dto.UserAdminVO;

/**
 * 管理端用户管理服务（软删除 / 修改角色 / 添加用户）
 */
public interface AdminUserService {

    /**
     * 用户列表（含已删除用户，含 status）。
     * page / pageSize 均传则分页返回，否则返回全部用户。
     */
    PageResult<UserAdminVO> listUsers(Integer page, Integer pageSize);

    /** 添加用户（可指定角色），返回新用户 id */
    Integer createUser(CreateUserRequest request);

    /** 修改用户角色 */
    void updateRole(Integer userId, String role);

    /** 软删除用户（幂等） */
    void softDelete(Integer userId);

    /** 恢复被软删除用户（幂等），恢复其登录权限 */
    void restore(Integer userId);
}
