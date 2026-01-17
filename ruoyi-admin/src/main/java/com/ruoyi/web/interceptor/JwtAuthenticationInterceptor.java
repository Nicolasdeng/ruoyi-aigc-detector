package com.ruoyi.web.interceptor;

import com.ruoyi.web.utils.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT认证拦截器
 * 负责从请求头中提取Token，解析userId并存储到request attribute中
 * 
 * @author ruoyi
 */
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationInterceptor.class);
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    /**
     * 用户ID在request attribute中的key
     */
    public static final String USER_ID_ATTRIBUTE = "userId";
    
    /**
     * Token请求头名称
     */
    private static final String TOKEN_HEADER = "Authorization";
    
    /**
     * Token前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头获取Token
        String token = extractToken(request);
        
        if (token != null && !token.isEmpty()) {
            try {
                // 验证Token并提取userId
                Long userId = jwtTokenUtil.getUserIdFromToken(token);
                
                if (userId != null) {
                    // 将userId存储到request attribute中
                    request.setAttribute(USER_ID_ATTRIBUTE, userId);
                    log.debug("JWT认证成功，userId: {}", userId);
                } else {
                    log.debug("Token有效但未包含userId");
                }
            } catch (Exception e) {
                // Token无效或已过期，不抛出异常，只记录日志
                // 是否需要登录由@RequiresAuth注解决定
                log.debug("Token解析失败: {}", e.getMessage());
            }
        } else {
            log.debug("请求未携带Token");
        }
        
        // 无论Token是否有效，都放行请求
        // 具体的认证检查由AuthCheckInterceptor完成
        return true;
    }
    
    /**
     * 从请求头中提取Token
     * 
     * @param request HTTP请求
     * @return Token字符串，如果不存在则返回null
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(TOKEN_HEADER);
        
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }
        
        return null;
    }
    
    /**
     * 从request中获取userId
     * 
     * @param request HTTP请求
     * @return userId，如果不存在则返回null
     */
    public static Long getUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute(USER_ID_ATTRIBUTE);
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        return null;
    }
}
