package com.imut.diab_health_sys02.service.impl;

import com.imut.diab_health_sys02.common.BizException;
import com.imut.diab_health_sys02.dto.*;
import com.imut.diab_health_sys02.entity.User;
import com.imut.diab_health_sys02.mapper.UserMapper;
import com.imut.diab_health_sys02.service.UserService;
import com.imut.diab_health_sys02.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[\\w\\u4e00-\\u9fa5-]{3,20}$");

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResult login(LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw BizException.badRequest("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw BizException.badRequest("密码不能为空");
        }
        User user = userMapper.findByUsernameAnyStatus(request.getUsername().trim());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BizException.unauthorized("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            // 账户已被软删除（冻结），禁止登录
            throw BizException.forbidden("账户存在异常请联系管理员");
        }
        return buildLoginResult(user);
    }

    @Override
    @Transactional
    public LoginResult register(RegisterRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw BizException.badRequest("用户名需为 3-20 位字母、数字、下划线或中文");
        }
        if (password.length() < 6 || password.length() > 32) {
            throw BizException.badRequest("密码长度需为 6-32 位");
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
        user.setRole("user");
        userMapper.insert(user);
        // 重新查询以获取数据库生成的 createdAt 等字段
        user = userMapper.findById(user.getUserId());
        return buildLoginResult(user);
    }

    @Override
    public UserInfoVO getUserInfo(Integer userId) {
        return UserInfoVO.from(requireUser(userId));
    }

    @Override
    public UserInfoVO updateUserInfo(Integer userId, UpdateUserRequest request) {
        User user = requireUser(userId);
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatarUrl(request.getAvatar());
        }
        if (request.getDesc() != null) {
            user.setDescription(request.getDesc());
        }
        userMapper.updateInfo(user);
        return UserInfoVO.from(user);
    }

    @Override
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
            throw BizException.badRequest("原密码不能为空");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6
                || request.getNewPassword().length() > 32) {
            throw BizException.badRequest("新密码长度需为 6-32 位");
        }
//        初步校验一下用户输入的新旧密码是否相同，而不是从数据库中拿
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw BizException.badRequest("新密码不能与原密码相同");
        }
        User user = requireUser(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw BizException.unauthorized("原密码错误");
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(request.getNewPassword()));
    }

    private User requireUser(Integer userId) {
        User user = userMapper.findById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() == 1)) {
            // 用户不存在或已被软删除
            throw BizException.notFound("用户不存在");
        }
        return user;
    }

    private LoginResult buildLoginResult(User user) {
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        return new LoginResult(token, UserInfoVO.from(user));
    }
}
