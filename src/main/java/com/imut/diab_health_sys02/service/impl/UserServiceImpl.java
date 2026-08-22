package com.imut.diab_health_sys02.service.impl;

import com.imut.diab_health_sys02.common.BizException;
import com.imut.diab_health_sys02.dto.ChangePasswordRequest;
import com.imut.diab_health_sys02.dto.LoginRequest;
import com.imut.diab_health_sys02.dto.LoginResult;
import com.imut.diab_health_sys02.dto.RegisterRequest;
import com.imut.diab_health_sys02.dto.UpdateUserRequest;
import com.imut.diab_health_sys02.dto.UserInfoVO;
import com.imut.diab_health_sys02.entity.User;
import com.imut.diab_health_sys02.entity.UserRiskInfo;
import com.imut.diab_health_sys02.mapper.UserMapper;
import com.imut.diab_health_sys02.mapper.UserRiskInfoMapper;
import com.imut.diab_health_sys02.service.UserService;
import com.imut.diab_health_sys02.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[\\w\\u4e00-\\u9fa5-]{3,20}$");

    private final UserMapper userMapper;
    private final UserRiskInfoMapper riskInfoMapper;
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
        UserInfoVO vo = UserInfoVO.from(requireUser(userId));
        vo.setHealthInfo(toHealthInfoMap(riskInfoMapper.findByUserId(userId)));
        return vo;
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
        // 健康档案落库：user_risk_info 表（与 Dify 工作流共用，userId 主键 upsert）
        if (request.getHealthInfo() != null && !request.getHealthInfo().isEmpty()) {
            riskInfoMapper.upsert(toUserRiskInfo(userId, request.getHealthInfo()));
        }
        return getUserInfo(userId);
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
        UserInfoVO vo = UserInfoVO.from(user);
        vo.setHealthInfo(toHealthInfoMap(riskInfoMapper.findByUserId(user.getUserId())));
        return new LoginResult(token, vo);
    }

    /** 实体 -> healthInfo Map（返回给前端）；无档案返回 null */
    private Map<String, Object> toHealthInfoMap(UserRiskInfo info) {
        if (info == null) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("disease", info.getDisease());
        map.put("diabetesType", info.getDiabetesType());
        map.put("sex", info.getSex());
        map.put("age", info.getAge());
        map.put("height", info.getHeight());
        map.put("weight", info.getWeight());
        map.put("familyHistory", info.getFamilyHistory());
        map.put("waistline", info.getWaistline());
        map.put("systolicPressure", info.getSystolicPressure());
        map.put("isPregnancy", info.getIsPregnancy());
        return map;
    }

    /** 请求体 healthInfo Map -> 实体（userId 主键，写 updated_at） */
    private UserRiskInfo toUserRiskInfo(Integer userId, Map<String, Object> h) {
        UserRiskInfo info = new UserRiskInfo();
        info.setUserId(userId);
        info.setDisease(asString(h.get("disease")));
        info.setDiabetesType(asString(h.get("diabetesType")));
        info.setSex(asString(h.get("sex")));
        info.setAge(asInteger(h.get("age")));
        info.setHeight(asDouble(h.get("height")));
        info.setWeight(asDouble(h.get("weight")));
        info.setFamilyHistory(asString(h.get("familyHistory")));
        info.setWaistline(asDouble(h.get("waistline")));
        info.setSystolicPressure(asDouble(h.get("systolicPressure")));
        info.setIsPregnancy(asString(h.get("isPregnancy")));
        info.setUpdatedAt(LocalDateTime.now());
        return info;
    }

    private String asString(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o).trim();
        return s.isEmpty() ? null : s;
    }

    private Integer asInteger(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        String s = String.valueOf(o).trim();
        if (s.isEmpty()) return null;
        try {
            return Double.valueOf(s).intValue();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double asDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).doubleValue();
        String s = String.valueOf(o).trim();
        if (s.isEmpty()) return null;
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
