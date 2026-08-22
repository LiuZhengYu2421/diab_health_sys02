package com.imut.diab_health_sys02.mapper;

import com.imut.diab_health_sys02.entity.DoctorInformation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 医师信息 Mapper（表 doctor_information，列名为下划线风格，
 * 依赖 mybatis.configuration.map-underscore-to-camel-case 自动映射到实体驼峰字段）
 */
@Mapper
public interface DoctorInformationMapper {

    /**
     * 按医生姓名查询医师信息（含 chat_token，用于调用 Dify 医师咨询助手）
     */
    @Select("SELECT info_id, doctor_name, department, title, introduction, image_url, chat_token " +
            "FROM doctor_information WHERE doctor_name = #{doctorName} LIMIT 1")
    DoctorInformation findByDoctorName(@Param("doctorName") String doctorName);

    /**
     * 医师列表（公开接口 GET /doctors，前端首页/查看全部复用；
     * 含可选科室筛选、姓名/简介关键词搜索）
     *
     * @param keyword    姓名或简介关键词（可空）
     * @param department 科室（可空）
     */
    @Select("<script>" +
            "SELECT info_id, doctor_name, department, title, introduction, image_url, chat_token " +
            "FROM doctor_information " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  (doctor_name LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR introduction LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "<if test='department != null and department != \"\"'>" +
            "  AND department = #{department}" +
            "</if>" +
            "</where> " +
            "ORDER BY info_id" +
            "</script>")
    List<DoctorInformation> findList(@Param("keyword") String keyword,
                                     @Param("department") String department);
}