package com.imut.diab_health_sys01.dto;

import lombok.Data;

/**
 * 打卡统计返回：{ streak, monthCount, totalCount }
 */
@Data
public class PunchStatsVO {

    /** 连续打卡天数 */
    private int streak;

    /** 本月打卡次数 */
    private int monthCount;

    /** 累计打卡次数 */
    private int totalCount;
}
