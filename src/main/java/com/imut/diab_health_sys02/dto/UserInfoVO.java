package com.imut.diab_health_sys02.dto;

import com.imut.diab_health_sys02.entity.User;
import lombok.Data;

import java.time.format.DateTimeFormatter;

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
