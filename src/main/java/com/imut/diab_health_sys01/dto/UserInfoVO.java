package com.imut.diab_health_sys01.dto;

import com.imut.diab_health_sys01.entity.User;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 返回给前端的用户信息（userInfo）
 */
@Data
public class UserInfoVO {

    private Integer id;

    private String username;

    private String nickname;

    private String avatar;

    private String desc;

    private String role;

    private String createdAt;

    /**
     * 糖尿病预测健康档案（来自 user_risk_info 表）。
     * 未填写时返回 null，前端按「未填写」优雅降级。
     */
    private Map<String, Object> healthInfo;

    public static UserInfoVO from(User user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatarUrl());
        vo.setDesc(user.getDescription());
        vo.setRole(user.getRole() == null ? "user" : user.getRole());
        vo.setCreatedAt(user.getCreatedAt() == null
                ? null
                : user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return vo;
    }
}
