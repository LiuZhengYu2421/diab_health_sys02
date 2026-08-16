package com.imut.diab_health_sys02.service;

import com.imut.diab_health_sys02.common.PageResult;
import com.imut.diab_health_sys02.dto.PunchStatsVO;
import com.imut.diab_health_sys02.entity.PunchIn;

/**
 * 打卡查询与统计服务（接口 40/41/42）
 */
public interface PunchInService {

    /** 分页查询我的打卡记录 */
    PageResult<PunchIn> list(Integer userId, int page, int pageSize,
                             String punchType, String startDate, String endDate);

    /** 打卡统计：连续天数 / 本月次数 / 累计次数 */
    PunchStatsVO stats(Integer userId);

    /** 删除自己的打卡记录（不存在或非本人返回 404） */
    void delete(Integer userId, Integer id);
}
