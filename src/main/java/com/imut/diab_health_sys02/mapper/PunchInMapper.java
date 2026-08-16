package com.imut.diab_health_sys02.mapper;

import com.imut.diab_health_sys02.entity.PunchIn;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 打卡记录 Mapper（人员二部分：分页查询 / 统计 / 删除；新增打卡 POST /punch-in 属人员三，另建）
 */
@Mapper
public interface PunchInMapper {

    /**
     * 分页查询我的打卡记录（支持 punchType、startDate~endDate 筛选）
     */
    @Select("<script>" +
            "SELECT id, user_id, punch_time, punch_type, completion_status, message FROM punch_in " +
            "WHERE user_id = #{userId} " +
            "<if test='punchType != null and punchType != \"\"'>AND punch_type = #{punchType} </if>" +
            "<if test='startDate != null and startDate != \"\"'>AND punch_time &gt;= CONCAT(#{startDate}, ' 00:00:00') </if>" +
            "<if test='endDate != null and endDate != \"\"'>AND punch_time &lt;= CONCAT(#{endDate}, ' 23:59:59') </if>" +
            "ORDER BY punch_time DESC LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<PunchIn> findPage(@Param("userId") Integer userId, @Param("punchType") String punchType,
                           @Param("startDate") String startDate, @Param("endDate") String endDate,
                           @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 统计总数（与 findPage 同条件）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM punch_in WHERE user_id = #{userId} " +
            "<if test='punchType != null and punchType != \"\"'>AND punch_type = #{punchType} </if>" +
            "<if test='startDate != null and startDate != \"\"'>AND punch_time &gt;= CONCAT(#{startDate}, ' 00:00:00') </if>" +
            "<if test='endDate != null and endDate != \"\"'>AND punch_time &lt;= CONCAT(#{endDate}, ' 23:59:59') </if>" +
            "</script>")
    long count(@Param("userId") Integer userId, @Param("punchType") String punchType,
               @Param("startDate") String startDate, @Param("endDate") String endDate);

    /** 累计打卡次数 */
    @Select("SELECT COUNT(*) FROM punch_in WHERE user_id = #{userId}")
    int countTotal(@Param("userId") Integer userId);

    /** 本月打卡次数 */
    @Select("SELECT COUNT(*) FROM punch_in WHERE user_id = #{userId} " +
            "AND DATE_FORMAT(punch_time, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')")
    int countMonth(@Param("userId") Integer userId);

    /** 打卡日期（按天去重，用于计算连续天数） */
    @Select("SELECT DISTINCT DATE(punch_time) FROM punch_in WHERE user_id = #{userId} ORDER BY punch_time DESC")
    List<LocalDate> findDistinctPunchDates(@Param("userId") Integer userId);

    /** 校验打卡记录归属（只能操作自己的） */
    @Select("SELECT id FROM punch_in WHERE id = #{id} AND user_id = #{userId}")
    Integer findOwn(@Param("id") Integer id, @Param("userId") Integer userId);

    /** 删除自己的打卡记录 */
    @Delete("DELETE FROM punch_in WHERE id = #{id} AND user_id = #{userId}")
    int deleteOwn(@Param("id") Integer id, @Param("userId") Integer userId);
}
