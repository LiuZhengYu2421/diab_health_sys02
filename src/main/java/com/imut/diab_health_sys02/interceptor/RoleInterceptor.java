package com.imut.diab_health_sys02.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imut.diab_health_sys02.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * 角色权限拦截器：拦截 /admin/** 管理端路径，
 * 要求当前登录用户 role=admin，否则按约定格式返回 403。
 * 注意：必须在 AuthInterceptor 之后注册（先完成认证，role 才会注入 request）。
 */
@Component
@RequiredArgsConstructor
public class RoleInterceptor implements HandlerInterceptor {

    private static final String ADMIN_ROLE = "admin";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object role = request.getAttribute(AuthInterceptor.ATTR_ROLE);
        if (!ADMIN_ROLE.equals(role)) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(objectMapper.writeValueAsString(Result.error(403, "没有权限执行该操作")));
            return false;
        }
        return true;
    }
}
