package com.imut.diab_health_sys01.service;

import com.imut.diab_health_sys01.dto.RiskInfoRequest;
import com.imut.diab_health_sys01.entity.UserRiskInfo;

/**
 * 基础糖尿病风险信息服务（接口 27/28）
 */
public interface RiskInfoService {

    /** 获取我的风险信息（未填写返回 null） */
    UserRiskInfo getRiskInfo(Integer userId);

    /** 提交/更新风险信息（有则覆盖，无则新增），返回 userId */
    Integer saveRiskInfo(Integer userId, RiskInfoRequest request);
}
