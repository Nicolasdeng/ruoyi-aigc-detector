package com.ruoyi.web.interceptor;

import com.ruoyi.web.annotation.RateLimit;
import com.ruoyi.web.utils.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流拦截器
 * 基于Redis实现分布式限流
 * 
 * @author ruoyi
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        
        if (rateLimit == null) {
            return true;
        }
        
        // 构建限流key
        String limitKey = buildLimitKey(request, rateLimit);
        
        // 获取当前访问次数
        Integer count = (Integer) redisTemplate.opsForValue().get(limitKey);
        
        if (count == null) {
            // 第一次访问，设置初始值和过期时间
            redisTemplate.opsForValue().set(limitKey, 1, rateLimit.time(), TimeUnit.SECONDS);
            return true;
        }
        
        if (count >= rateLimit.count()) {
            // 超过限流次数
            log.warn("接口限流触发：{} - 限流key: {}, 当前次数: {}, 限制次数: {}", 
                    request.getRequestURI(), limitKey, count, rateLimit.count());
            
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"msg\":\"访问过于频繁，请稍后再试\"}");
            return false;
        }
        
        // 增加访问次数
        redisTemplate.opsForValue().increment(limitKey);
        return true;
    }
    
    /**
     * 构建限流key
     */
    private String buildLimitKey(HttpServletRequest request, RateLimit rateLimit) {
        StringBuilder key = new StringBuilder(rateLimit.key());
        
        switch (rateLimit.limitType()) {
            case IP:
                key.append(getIpAddress(request));
                break;
            case USER:
                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    try {
                        Long userId = jwtTokenUtil.getUserIdFromToken(token);
                        key.append(userId);
                    } catch (Exception e) {
                        key.append(getIpAddress(request));
                    }
                } else {
                    key.append(getIpAddress(request));
                }
                break;
            default:
                key.append(getIpAddress(request));
                break;
        }
        
        key.append(":").append(request.getRequestURI());
        return key.toString();
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
