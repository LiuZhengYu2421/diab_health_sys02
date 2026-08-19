package com.imut.diab_health_sys01.service;

import com.imut.diab_health_sys01.common.PageResult;
import com.imut.diab_health_sys01.dto.PunchCreateRequest;
import com.imut.diab_health_sys01.dto.PunchStatsVO;
import com.imut.diab_health_sys01.entity.PunchIn;

/**
 * 打卡服务（新增 / 分页查询 / 统计 / 删除）
 */
public interface PunchInService {

    /** 新增打卡（punch_time 服务端生成，返回完整记录） */
    PunchIn create(Integer userId, PunchCreateRequest request);

    /** 分页查询我的打卡记录 */
    PageResult<PunchIn> list(Integer userId, int page, int pageSize,
                             String punchType, String startDate, String endDate);

    /** 打卡统计：连续天数 / 本月次数 / 累计次数 */
    PunchStatsVO stats(Integer userId);

    /** 删除自己的打卡记录（不存在或非本人返回 404） */
    void delete(Integer userId, Integer id);
}
