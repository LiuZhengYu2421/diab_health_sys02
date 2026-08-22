package com.imut.diab_health_sys02.service.impl;

import com.imut.diab_health_sys02.dto.RiskInfoRequest;
import com.imut.diab_health_sys02.entity.UserRiskInfo;
import com.imut.diab_health_sys02.mapper.UserRiskInfoMapper;
import com.imut.diab_health_sys02.service.RiskInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RiskInfoServiceImpl implements RiskInfoService {

    private final UserRiskInfoMapper riskInfoMapper;

    @Override
    public UserRiskInfo getRiskInfo(Integer userId) {
        return riskInfoMapper.findByUserId(userId);
    }

    @Override
    public Integer saveRiskInfo(Integer userId, RiskInfoRequest request) {
        UserRiskInfo info = new UserRiskInfo();
        info.setUserId(userId);
        info.setAge(request.getAge());
        info.setSex(request.getSex());
        info.setHeight(request.getHeight());
        info.setWeight(request.getWeight());
        info.setFamilyHistory(request.getFamilyHistory());
        info.setWaistline(request.getWaistline());
        info.setSystolicPressure(request.getSystolicPressure());
        info.setIsPregnancy(request.getIsPregnancy());
        info.setMessage(request.getMessage());
        info.setDisease(request.getDisease());
        info.setDiabetesType(request.getDiabetesType());
        info.setUpdatedAt(LocalDateTime.now());
        riskInfoMapper.upsert(info);
        return userId;
    }
}
