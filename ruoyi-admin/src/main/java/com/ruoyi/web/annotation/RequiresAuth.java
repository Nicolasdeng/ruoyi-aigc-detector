package com.ruoyi.web.annotation;

import java.lang.annotation.*;

/**
 * 需要登录认证注解
 * 标记在Controller的类或方法上，表示该接口需要登录后才能访问
 * 
 * 使用方式：
 * 1. 标记在类上：整个Controller的所有方法都需要登录
 * 2. 标记在方法上：单个方法需要登录
 * 3. required=false：可选登录，有Token则提取userId，无Token也放行
 * 4. required=true（默认）：强制登录，无Token或Token无效则返回401
 * 
 * @author ruoyi
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresAuth {
    
    /**
     * 是否必须登录
     * true - 强制登录，无Token或Token无效则返回401错误
     * false - 可选登录，有Token则提取userId，无Token也允许访问
     * 
     * @return 默认true，即强制登录
     */
    boolean required() default true;
}
