package com.imut.diab_health_sys01.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imut.diab_health_sys01.common.Result;
import com.imut.diab_health_sys01.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * JWT 认证拦截器：解析 Authorization: Bearer <token>，
 * 校验通过后将 userId / username / role 写入 request attribute 供 Controller 使用；
 * 失败时按约定格式返回 401。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USER_ID = "currentUserId";
    public static final String ATTR_USERNAME = "currentUsername";
    public static final String ATTR_ROLE = "currentRole";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 公开浏览接口（后端接口文档 11.2 方案一）：医生/文章/糖尿病类型 的列表与详情无需登录
        if (isPublicRequest(request)) {
            return true;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }
        String token = header.substring(7).trim();
//        从0-6截断 7开始录入
//        System.out.println(token);
        Map<String, Object> payload = jwtUtil.parseToken(token);

        if (payload == null || payload.get("userId") == null) {
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }
        request.setAttribute(ATTR_USER_ID, payload.get("userId"));
        request.setAttribute(ATTR_USERNAME, payload.get("username"));
        request.setAttribute(ATTR_ROLE, payload.get("role"));
        return true;
    }

    /**
     * 公开浏览接口判断（无需登录，对应后端接口文档 11.2 方案一）：
     * GET /doctors、/doctors/{id}、/articles、/articles/categories、/articles/{id}、
     * /diabetes-types、/diabetes-types/{id}
     * 注意：文章收藏相关接口（/articles/favorites、/articles/{id}/favorite/status 等）仍需登录。
     */
    private boolean isPublicRequest(HttpServletRequest request) {
        if (!"GET".equals(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            uri = uri.substring(ctx.length());
        }
        if (uri.equals("/doctors") || uri.matches("/doctors/\\d+")) {
            return true;
        }
        if (uri.equals("/articles") || uri.equals("/articles/categories") || uri.matches("/articles/\\d+")) {
            return true;
        }
        if (uri.equals("/diabetes-types") || uri.matches("/diabetes-types/\\d+")) {
            return true;
        }
        return false;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, message)));
    }
}
