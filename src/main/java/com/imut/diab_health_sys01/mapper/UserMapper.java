package com.imut.diab_health_sys01.mapper;

import com.imut.diab_health_sys01.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户表 Mapper
 */
@Mapper
public interface UserMapper {

    /** 按用户名查询【活跃】用户（已软删除 status=1 的用户不可注册查重拦截） */
    @Select("SELECT user_id, username, nickname, description, password, avatar_url, role, status, created_at " +
            "FROM users WHERE username = #{username} AND status = 0")
    User findByUsername(@Param("username") String username);

    /** 按用户名查询任意状态用户（含已软删除，登录时用于区分"密码错误"与"账户被冻结"） */
    @Select("SELECT user_id, username, nickname, description, password, avatar_url, role, status, created_at " +
            "FROM users WHERE username = #{username}")
    User findByUsernameAnyStatus(@Param("username") String username);

    /** 全部用户（含已删除，管理端列表） */
    @Select("SELECT user_id, username, nickname, description, password, avatar_url, role, status, created_at " +
            "FROM users ORDER BY user_id")
    List<User> findAll();

    /** 按 id 查询任意用户（含已删除，管理端使用） */
    @Select("SELECT user_id, username, nickname, description, password, avatar_url, role, status, created_at " +
            "FROM users WHERE user_id = #{userId}")
    User findById(@Param("userId") Integer userId);

    /** 新增用户（role 由调用方指定：注册传 user，管理端传指定角色） */
    @Insert("INSERT INTO users (username, nickname, password, avatar_url, role) " +
            "VALUES (#{username}, #{nickname}, #{password}, #{avatarUrl}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insert(User user);

    @Update("UPDATE users SET nickname = #{nickname}, avatar_url = #{avatarUrl}, description = #{description} " +
            "WHERE user_id = #{userId}")
    int updateInfo(User user);

    @Update("UPDATE users SET password = #{password} WHERE user_id = #{userId}")
    int updatePassword(@Param("userId") Integer userId, @Param("password") String password);

    /** 修改用户角色（管理端） */
    @Update("UPDATE users SET role = #{role} WHERE user_id = #{userId}")
    int updateRole(@Param("userId") Integer userId, @Param("role") String role);

    /** 软删除用户（管理端）：status 置 1，幂等 */
    @Update("UPDATE users SET status = 1 WHERE user_id = #{userId}")
    int softDelete(@Param("userId") Integer userId);

    /** 恢复被软删除用户（管理端）：status 置 0，恢复登录权限，幂等 */
    @Update("UPDATE users SET status = 0 WHERE user_id = #{userId}")
    int restore(@Param("userId") Integer userId);
}
