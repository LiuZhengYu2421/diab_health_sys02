package com.imut.diab_health_sys02.controller;

import com.imut.diab_health_sys02.common.PageResult;
import com.imut.diab_health_sys02.common.Result;
import com.imut.diab_health_sys02.entity.DoctorInformation;
import com.imut.diab_health_sys02.mapper.DoctorInformationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 医师团队（公开接口，GET /doctors 已由 AuthInterceptor 放行，无需登录）
 * 前端：src/api/admin.js getDoctors()
 * 返回结构：Result.success(PageResult) → { code:200, data: { list, total } }
 */
@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorInformationMapper doctorInformationMapper;

    /**
     * 医师列表（分页 + 姓名/简介搜索 + 科室筛选）
     *
     * @param page      页码（默认 1）
     * @param pageSize  每页条数（默认 10）
     * @param keyword   姓名或简介关键词（可空）
     * @param department 科室（可空）
     */
    @GetMapping
    public Result<PageResult<DoctorInformation>> list(@RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String department) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
        String dept = StringUtils.hasText(department) ? department.trim() : null;
        List<DoctorInformation> all = doctorInformationMapper.findList(kw, dept);
        int total = all.size();
        int from = Math.min((page - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        List<DoctorInformation> pageList = all.subList(from, to);
        // 对外不暴露 chat_token（该凭据仅后端按 doctor_name 查询使用）
        pageList.forEach(d -> d.setChatToken(null));
        return Result.success(PageResult.of(pageList, total, page, pageSize));
    }
}