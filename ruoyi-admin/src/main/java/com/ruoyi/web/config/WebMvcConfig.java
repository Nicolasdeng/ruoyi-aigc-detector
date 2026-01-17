package com.ruoyi.web.config;

import com.ruoyi.web.interceptor.RateLimitInterceptor;
import com.ruoyi.web.interceptor.JwtAuthenticationInterceptor;
import com.ruoyi.web.interceptor.AuthCheckInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * 
 * @author ruoyi
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Autowired
    private AuthCheckInterceptor authCheckInterceptor;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. JWT认证拦截器 - 最先执行，提取userId
        // 排除登录接口，因为登录时还没有token
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/wechat/**", "/ai/**", "/paper/**", "/riddle/**")
                .excludePathPatterns("/wechat/login")
                .order(0);

        // 2. 认证检查拦截器 - 第二执行，检查@RequiresAuth注解
        // 排除登录接口，因为登录接口不需要认证
        registry.addInterceptor(authCheckInterceptor)
                .addPathPatterns("/wechat/**", "/ai/**", "/paper/**", "/riddle/**")
                .excludePathPatterns("/wechat/login")
                .order(1);

        // 3. 限流拦截器 - 最后执行
        // 限流拦截器仍然需要拦截登录接口，防止暴力破解
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/wechat/**", "/ai/**", "/paper/**", "/riddle/**")
                .order(2);
    }
}
