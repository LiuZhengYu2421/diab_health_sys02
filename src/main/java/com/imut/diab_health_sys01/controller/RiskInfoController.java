package com.imut.diab_health_sys01.controller;

import com.imut.diab_health_sys01.common.Result;
import com.imut.diab_health_sys01.dto.RiskInfoRequest;
import com.imut.diab_health_sys01.entity.UserRiskInfo;
import com.imut.diab_health_sys01.interceptor.AuthInterceptor;
import com.imut.diab_health_sys01.service.RiskInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 基础糖尿病风险预测（需登录，接口 27/28）
 */
@RestController
@RequestMapping("/risk-info")
@RequiredArgsConstructor
public class RiskInfoController {

    private final RiskInfoService riskInfoService;

    /** 获取我的风险信息（未填写返回 data:null） */
    @GetMapping
    public Result<UserRiskInfo> getRiskInfo(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Integer userId) {
        return Result.success(riskInfoService.getRiskInfo(userId));
    }

    /** 提交/更新风险信息（有则覆盖，无则新增） */
    @PostMapping
    public Result<Map<String, Object>> saveRiskInfo(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Integer userId,
                                                    @RequestBody RiskInfoRequest request) {
        Integer saved = riskInfoService.saveRiskInfo(userId, request);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", saved);
        return Result.success(data);
    }
}
