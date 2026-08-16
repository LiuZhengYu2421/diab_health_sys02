package com.imut.diab_health_sys02.service.impl;

import com.imut.diab_health_sys02.common.BizException;
import com.imut.diab_health_sys02.common.PageResult;
import com.imut.diab_health_sys02.dto.PunchStatsVO;
import com.imut.diab_health_sys02.entity.PunchIn;
import com.imut.diab_health_sys02.mapper.PunchInMapper;
import com.imut.diab_health_sys02.service.PunchInService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PunchInServiceImpl implements PunchInService {

    private final PunchInMapper punchInMapper;

    @Override
    public PageResult<PunchIn> list(Integer userId, int page, int pageSize,
                                    String punchType, String startDate, String endDate) {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        int offset = (page - 1) * pageSize;
        List<PunchIn> list = punchInMapper.findPage(userId, punchType, startDate, endDate, offset, pageSize);
        long total = punchInMapper.count(userId, punchType, startDate, endDate);
        return PageResult.of(list, total, page, pageSize);
    }

    @Override
    public PunchStatsVO stats(Integer userId) {
        PunchStatsVO vo = new PunchStatsVO();
        vo.setTotalCount(punchInMapper.countTotal(userId));
        vo.setMonthCount(punchInMapper.countMonth(userId));
        vo.setStreak(calcStreak(punchInMapper.findDistinctPunchDates(userId)));
        return vo;
    }

    @Override
    public void delete(Integer userId, Integer id) {
        if (punchInMapper.findOwn(id, userId) == null) {
            throw BizException.notFound("打卡记录不存在");
        }
        punchInMapper.deleteOwn(id, userId);
    }

    /**
     * 连续打卡天数：从今天（今天没打卡则从昨天）起往前数连续有打卡的天数
     */
    private int calcStreak(List<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            return 0;
        }
        LocalDate cur = LocalDate.now();
        if (!dates.contains(cur)) {
            cur = cur.minusDays(1);
        }
        int streak = 0;
        while (dates.contains(cur)) {
            streak++;
            cur = cur.minusDays(1);
        }
        return streak;
    }
}
