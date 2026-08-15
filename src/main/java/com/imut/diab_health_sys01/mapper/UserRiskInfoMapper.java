package com.imut.diab_health_sys01.mapper;

import com.imut.diab_health_sys01.entity.UserRiskInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户风险信息 Mapper（表 user_risk_info，主键列名为驼峰 userId）
 */
@Mapper
public interface UserRiskInfoMapper {

    @Select("SELECT userId, age, sex, height, weight, familyHistory, waistline, " +
            "systolicPressure, isPregnancy, message, disease " +
            "FROM user_risk_info WHERE userId = #{userId}")
    UserRiskInfo findByUserId(@Param("userId") Integer userId);

    /**
     * 有则覆盖更新、无则新增（以 userId 为主键判断）
     */
    @Insert("INSERT INTO user_risk_info (userId, age, sex, height, weight, familyHistory, waistline, " +
            "systolicPressure, isPregnancy, message, disease) " +
            "VALUES (#{userId}, #{age}, #{sex}, #{height}, #{weight}, #{familyHistory}, #{waistline}, " +
            "#{systolicPressure}, #{isPregnancy}, #{message}, #{disease}) " +
            "ON DUPLICATE KEY UPDATE age = VALUES(age), sex = VALUES(sex), height = VALUES(height), " +
            "weight = VALUES(weight), familyHistory = VALUES(familyHistory), waistline = VALUES(waistline), " +
            "systolicPressure = VALUES(systolicPressure), isPregnancy = VALUES(isPregnancy), " +
            "message = VALUES(message), disease = VALUES(disease)")
    int upsert(UserRiskInfo info);
}
