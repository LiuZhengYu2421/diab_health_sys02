package com.imut.diab_health_sys02.controller;

import com.imut.diab_health_sys02.common.Result;
import com.imut.diab_health_sys02.dto.CreateUserRequest;
import com.imut.diab_health_sys02.dto.UpdateRoleRequest;
import com.imut.diab_health_sys02.dto.UserAdminVO;
import com.imut.diab_health_sys02.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端用户信息管理（/admin/**，由 RoleInterceptor 校验 role=admin）
 * - 用户列表（含已删除，含 status）
 * - 添加用户
 * - 修改用户角色
 * - 软删除用户
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /** 用户列表（含已删除用户，含 status） */
    @GetMapping
    public Result<List<UserAdminVO>> listUsers() {
        return Result.success(adminUserService.listUsers());
    }

    /** 添加用户（可指定角色） */
    @PostMapping
    public Result<Map<String, Object>> createUser(@RequestBody CreateUserRequest request) {
        Integer userId = adminUserService.createUser(request);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        return Result.success("添加成功", data);
    }

    /** 修改用户角色 */
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable("id") Integer id,
                                   @RequestBody UpdateRoleRequest request) {
        adminUserService.updateRole(id, request.getRole());
        return Result.success("修改成功", null);
    }

    /** 软删除用户（幂等） */
    @DeleteMapping("/{id}")
    public Result<Void> softDelete(@PathVariable("id") Integer id) {
        adminUserService.softDelete(id);
        return Result.success("删除成功", null);
    }

    /** 恢复被软删除用户（幂等），恢复其登录权限 */
    @PutMapping("/{id}/restore")
    public Result<Void> restore(@PathVariable("id") Integer id) {
        adminUserService.restore(id);
        return Result.success("恢复成功", null);
    }
}
