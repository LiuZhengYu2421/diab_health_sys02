package com.imut.diab_health_sys02.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 简易 JWT 工具（HS256），仅依赖 JDK 标准库与 Jackson，无需额外引入 jjwt。
 * Payload: { userId, username, role, exp }
 */
@Component
public class JwtUtil {

    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    @Value("${jwt.secret:diab-health-sys-jwt-secret-2026}")
    private String secret;

    @Value("${jwt.expire-hours:168}")
    private long expireHours;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private byte[] keyBytes;

    @PostConstruct
    public void init() {
        this.keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 生成 token
     */
    public String generateToken(Integer userId, String username, String role) {
        long exp = System.currentTimeMillis() / 1000 + expireHours * 3600;
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("role", role);
        payload.put("exp", exp);
        try {
            String header = base64Url(HEADER.getBytes(StandardCharsets.UTF_8));
            String body = base64Url(objectMapper.writeValueAsBytes(payload));
            String signature = sign(header + "." + body);
            return header + "." + body + "." + signature;
        } catch (Exception e) {
            throw new IllegalStateException("生成 token 失败", e);
        }
    }

    /**
     * 解析 token，返回 payload；签名非法或过期时返回 null
     */
    public Map<String, Object> parseToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }
        String header = parts[0];
        String body = parts[1];
        String signature = parts[2];
        try {
            // 校验签名
            String expect = sign(header + "." + body);
            if (!constantTimeEquals(expect, signature)) {
                return null;
            }
            byte[] payloadBytes = Base64.getUrlDecoder().decode(body);
            Map<String, Object> payload = objectMapper.readValue(payloadBytes, Map.class);
            // 校验过期
            Object exp = payload.get("exp");
            if (exp == null) {
                return null;
            }
            long expSeconds = ((Number) exp).longValue();
            if (System.currentTimeMillis() / 1000 >= expSeconds) {
                return null;
            }
            return payload;
        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
        return base64Url(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
