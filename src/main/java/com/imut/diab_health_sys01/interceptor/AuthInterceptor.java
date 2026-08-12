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
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }
        String token = header.substring(7).trim();
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

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, message)));
    }
}
