package com.imut.diab_health_sys01.dto;

import com.imut.diab_health_sys01.entity.User;
import lombok.Data;

import java.time.format.DateTimeFormatter;

/**
 * 管理端用户列表视图（含 status，不含密码）
 */
@Data
public class UserAdminVO {

    private Integer id;

    private String username;

    private String nickname;

    private String role;

    /** 状态：0 正常 / 1 已删除 */
    private Integer status;

    private String createdAt;

    public static UserAdminVO from(User user) {
        UserAdminVO vo = new UserAdminVO();
        vo.setId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus() == null ? 0 : user.getStatus());
        vo.setCreatedAt(user.getCreatedAt() == null
                ? null
                : user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return vo;
    }
}
