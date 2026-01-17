package com.ruoyi.web.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 用于控制接口访问频率，防止恶意刷接口
 * 
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    
    /**
     * 限流key前缀
     */
    String key() default "rate_limit:";
    
    /**
     * 时间窗口，单位秒
     */
    int time() default 60;
    
    /**
     * 时间窗口内最大请求次数
     */
    int count() default 10;
    
    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.DEFAULT;
    
    /**
     * 限流类型枚举
     */
    enum LimitType {
        /**
         * 默认策略：根据请求者IP限流
         */
        DEFAULT,
        
        /**
         * 根据用户ID限流
         */
        USER,
        
        /**
         * 根据IP限流
         */
        IP
    }
}
