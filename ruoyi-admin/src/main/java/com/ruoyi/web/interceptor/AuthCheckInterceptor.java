package com.ruoyi.web.interceptor;

import com.ruoyi.web.annotation.RequiresAuth;
import com.ruoyi.web.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 认证检查拦截器
 * 
 * <p>功能说明：
 * 1. 检查方法/类上的@RequiresAuth注解
 * 2. 如果required=true且userId为null，抛出UnauthorizedException（401错误）
 * 3. 如果required=false或无@RequiresAuth注解，允许访问
 * 
 * <p>使用说明：
 * - 在Controller类上添加@RequiresAuth：该类所有方法都需要登录
 * - 在Controller方法上添加@RequiresAuth：该方法需要登录（会覆盖类级别的注解）
 * - @RequiresAuth(required=false)：可选登录，有Token则提取userId，无Token也放行
 * - 无@RequiresAuth注解：默认不需要登录
 * 
 * <p>注意事项：
 * - 此拦截器必须在JwtAuthenticationInterceptor之后执行
 * - JwtAuthenticationInterceptor负责从Token提取userId并存储到request attribute
 * - 此拦截器负责检查userId是否存在以及是否需要强制登录
 * 
 * @author ruoyi
 * @date 2026-01-11
 */
@Slf4j
@Component
public class AuthCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理Controller方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Class<?> clazz = method.getDeclaringClass();

        // 1. 先检查方法级别的@RequiresAuth注解
        RequiresAuth methodAuth = method.getAnnotation(RequiresAuth.class);
        
        // 2. 如果方法上没有，检查类级别的@RequiresAuth注解
        RequiresAuth classAuth = clazz.getAnnotation(RequiresAuth.class);
        
        // 3. 确定最终的认证要求（方法级别优先于类级别）
        RequiresAuth effectiveAuth = methodAuth != null ? methodAuth : classAuth;
        
        // 4. 如果没有@RequiresAuth注解，默认不需要登录，直接放行
        if (effectiveAuth == null) {
            log.debug("接口无需登录: {} {}", request.getMethod(), request.getRequestURI());
            return true;
        }

        // 5. 从request attribute获取userId（由JwtAuthenticationInterceptor设置）
        Long userId = JwtAuthenticationInterceptor.getUserId(request);
        
        // 6. 根据required参数决定是否强制登录
        if (effectiveAuth.required()) {
            // 强制登录：userId必须存在
            if (userId == null) {
                log.warn("接口需要登录但未提供有效Token: {} {}", request.getMethod(), request.getRequestURI());
                throw new UnauthorizedException("请先登录");
            }
            log.debug("接口认证成功: {} {}, userId: {}", request.getMethod(), request.getRequestURI(), userId);
        } else {
            // 可选登录：有userId更好，没有也放行
            if (userId != null) {
                log.debug("接口可选登录，已登录: {} {}, userId: {}", request.getMethod(), request.getRequestURI(), userId);
            } else {
                log.debug("接口可选登录，未登录: {} {}", request.getMethod(), request.getRequestURI());
            }
        }

        return true;
    }
}
