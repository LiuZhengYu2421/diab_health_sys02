package com.imut.diab_health_sys02.service.impl;

import com.imut.diab_health_sys02.common.BizException;
import com.imut.diab_health_sys02.dto.CreateUserRequest;
import com.imut.diab_health_sys02.dto.UserAdminVO;
import com.imut.diab_health_sys02.entity.User;
import com.imut.diab_health_sys02.mapper.UserMapper;
import com.imut.diab_health_sys02.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[\\w\\u4e00-\\u9fa5-]{3,20}$");
    private static final List<String> ROLES = Arrays.asList("user", "doctor", "admin");

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserAdminVO> listUsers() {
        return userMapper.findAll().stream()
                .map(UserAdminVO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Integer createUser(CreateUserRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();
        String role = request.getRole() == null || request.getRole().trim().isEmpty()
                ? "user" : request.getRole().trim();
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw BizException.badRequest("用户名需为 3-20 位字母、数字、下划线或中文");
        }
        if (password.length() < 6 || password.length() > 32) {
            throw BizException.badRequest("密码长度需为 6-32 位");
        }
        if (!ROLES.contains(role)) {
            throw BizException.badRequest("角色只能为 user / doctor / admin");
        }
        if (userMapper.findByUsername(username) != null) {
            throw BizException.conflict("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setNickname(request.getNickname() == null || request.getNickname().trim().isEmpty()
                ? username : request.getNickname().trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setAvatarUrl("/img/user_icon.png");
        user.setRole(role);
        userMapper.insert(user);
        return user.getUserId();
    }

    @Override
    public void updateRole(Integer userId, String role) {
        if (role == null || !ROLES.contains(role)) {
            throw BizException.badRequest("角色只能为 user / doctor / admin");
        }
        requireExists(userId);
        userMapper.updateRole(userId, role);
    }

    @Override
    public void softDelete(Integer userId) {
        requireExists(userId);
        // 幂等：重复删除也返回成功
        userMapper.softDelete(userId);
    }

    @Override
    public void restore(Integer userId) {
        requireExists(userId);
        // 幂等：仅对已删除用户恢复，活跃用户重复恢复也返回成功
        userMapper.restore(userId);
    }

    private void requireExists(Integer userId) {
        if (userMapper.findById(userId) == null) {
            throw BizException.notFound("用户不存在");
        }
    }
}
