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

    @Select("SELECT user_id, username, nickname, description, password, avatar_url, role, created_at " +
            "FROM users WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Select("SELECT user_id, username, nickname, description, password, avatar_url, role, created_at " +
            "FROM users ORDER BY user_id")
    List<User> findAll();

    @Select("SELECT user_id, username, nickname, description, password, avatar_url, role, created_at " +
            "FROM users WHERE user_id = #{userId}")
    User findById(@Param("userId") Integer userId);

    @Insert("INSERT INTO users (username, nickname, password, avatar_url, role) " +
            "VALUES (#{username}, #{nickname}, #{password}, #{avatarUrl}, 'user')")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insert(User user);

    @Update("UPDATE users SET nickname = #{nickname}, avatar_url = #{avatarUrl}, description = #{description} " +
            "WHERE user_id = #{userId}")
    int updateInfo(User user);

    @Update("UPDATE users SET password = #{password} WHERE user_id = #{userId}")
    int updatePassword(@Param("userId") Integer userId, @Param("password") String password);
}
