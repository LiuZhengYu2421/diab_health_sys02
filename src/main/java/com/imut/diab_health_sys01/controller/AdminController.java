package com.imut.diab_health_sys01.controller;

import com.imut.diab_health_sys01.common.Result;
import com.imut.diab_health_sys01.dto.UserInfoVO;
import com.imut.diab_health_sys01.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理端接口（/admin/**，由 RoleInterceptor 校验 role=admin）
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;

    /** 管理端：用户列表（脱敏，不含密码） */
    @GetMapping("/users")
    public Result<List<UserInfoVO>> listUsers() {
        List<UserInfoVO> list = userMapper.findAll().stream()
                .map(UserInfoVO::from)
                .collect(Collectors.toList());
        return Result.success(list);
    }
}
