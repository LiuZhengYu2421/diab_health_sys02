package com.imut.diab_health_sys02.controller;

import com.imut.diab_health_sys02.common.PageResult;
import com.imut.diab_health_sys02.common.Result;
import com.imut.diab_health_sys02.dto.PunchStatsVO;
import com.imut.diab_health_sys02.entity.PunchIn;
import com.imut.diab_health_sys02.interceptor.AuthInterceptor;
import com.imut.diab_health_sys02.service.PunchInService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 打卡记录查询与统计分析（需登录，接口 40/41/42）
 * 注：新增打卡 POST /punch-in 属人员三负责，此处不实现
 */
@RestController
@RequestMapping("/punch-in")
@RequiredArgsConstructor
public class PunchInController {

    private final PunchInService punchInService;

    /** 打卡记录列表（分页 + 类型 + 日期范围筛选） */
    @GetMapping
    public Result<PageResult<PunchIn>> list(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Integer userId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) String punchType,
                                            @RequestParam(required = false) String startDate,
                                            @RequestParam(required = false) String endDate) {
        return Result.success(punchInService.list(userId, page, pageSize, punchType, startDate, endDate));
    }

    /** 打卡统计（连续天数 / 本月次数 / 累计次数） */
    @GetMapping("/stats")
    public Result<PunchStatsVO> stats(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Integer userId) {
        return Result.success(punchInService.stats(userId));
    }

    /** 删除打卡记录（只能删自己的） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Integer userId,
                               @PathVariable("id") Integer id) {
        punchInService.delete(userId, id);
        return Result.success(null);
    }
}
