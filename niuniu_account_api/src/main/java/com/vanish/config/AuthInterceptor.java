package com.vanish.config;

import com.vanish.common.exception.BusinessException;
import com.vanish.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：校验 Authorization: Bearer <token>，解析后将 userId 放入请求属性
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(401, "未登录或 token 缺失");
        }
        String token = authHeader.substring(BEARER_PREFIX.length());
        String userId = jwtUtil.parseToken(token);
        request.setAttribute("userId", userId);
        return true;
    }
}
