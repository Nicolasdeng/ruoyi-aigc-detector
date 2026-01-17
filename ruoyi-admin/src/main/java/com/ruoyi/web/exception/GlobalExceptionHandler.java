package com.ruoyi.web.exception;

import com.ruoyi.common.core.domain.AjaxResult;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理系统中的各类异常，提供友好的错误提示
 * 
 * @author ruoyi
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Token过期异常
     */
    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public AjaxResult handleExpiredJwtException(ExpiredJwtException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}', Token已过期: {}", requestURI, e.getMessage());
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorCode", "TOKEN_EXPIRED");
        data.put("message", "登录已过期，请重新登录");
        
        return AjaxResult.error("Token已过期", data);
    }
    
    /**
     * Token格式错误异常
     */
    @ExceptionHandler(MalformedJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public AjaxResult handleMalformedJwtException(MalformedJwtException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}', Token格式错误: {}", requestURI, e.getMessage());
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorCode", "TOKEN_MALFORMED");
        data.put("message", "Token格式错误，请重新登录");
        
        return AjaxResult.error("Token格式错误", data);
    }
    
    /**
     * Token签名验证失败异常
     */
    @ExceptionHandler(SignatureException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public AjaxResult handleSignatureException(SignatureException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}', Token签名验证失败: {}", requestURI, e.getMessage());
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorCode", "TOKEN_INVALID");
        data.put("message", "Token无效，请重新登录");
        
        return AjaxResult.error("Token签名验证失败", data);
    }
    
    /**
     * Token在黑名单中异常
     */
    @ExceptionHandler(TokenBlacklistedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public AjaxResult handleTokenBlacklistedException(TokenBlacklistedException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}', Token已被加入黑名单: {}", requestURI, e.getMessage());
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorCode", "TOKEN_BLACKLISTED");
        data.put("message", "Token已失效，请重新登录");
        data.put("reason", e.getMessage());
        
        return AjaxResult.error("Token已失效", data);
    }
    
    /**
     * 接口限流异常
     */
    @ExceptionHandler(RateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public AjaxResult handleRateLimitException(RateLimitException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}', 访问频率超过限制: {}", requestURI, e.getMessage());
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorCode", "RATE_LIMIT_EXCEEDED");
        data.put("message", "访问过于频繁，请稍后再试");
        data.put("retryAfter", e.getRetryAfter());
        
        return AjaxResult.error("访问频率超过限制", data);
    }
    
    /**
     * 用户未登录异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public AjaxResult handleUnauthorizedException(UnauthorizedException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}', 用户未登录: {}", requestURI, e.getMessage());
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorCode", "UNAUTHORIZED");
        data.put("message", "请先登录");
        
        return AjaxResult.error("未登录或登录已过期", data);
    }
    
    /**
     * 权限不足异常
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public AjaxResult handleForbiddenException(ForbiddenException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}', 权限不足: {}", requestURI, e.getMessage());
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorCode", "FORBIDDEN");
        data.put("message", "权限不足，无法访问");
        
        return AjaxResult.error("权限不足", data);
    }
    
    /**
     * 微信API调用异常
     */
    @ExceptionHandler(WechatApiException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public AjaxResult handleWechatApiException(WechatApiException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}', 微信API调用失败: {}", requestURI, e.getMessage(), e);
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorCode", "WECHAT_API_ERROR");
        data.put("message", "微信服务暂时不可用，请稍后再试");
        data.put("wechatErrcode", e.getErrcode());
        
        return AjaxResult.error("微信登录失败", data);
    }
    
    /**
     * 参数校验异常（@Validated）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AjaxResult handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}', 参数校验失败: {}", requestURI, e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorCode", "VALIDATION_ERROR");
        data.put("message", "参数校验失败");
        data.put("errors", errors);
        
        return AjaxResult.error("参数校验失败", data);
    }
    
    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AjaxResult handleBindException(BindException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}', 参数绑定失败: {}", requestURI, e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorCode", "BIND_ERROR");
        data.put("message", "参数绑定失败");
        data.put("errors", errors);
        
        return AjaxResult.error("参数错误", data);
    }
    
    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AjaxResult handleBusinessException(BusinessException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}', 业务异常: {}", requestURI, e.getMessage());
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorCode", e.getCode());
        data.put("message", e.getMessage());
        
        return AjaxResult.error(e.getMessage(), data);
    }
    
    /**
     * 空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public AjaxResult handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}', 发生空指针异常", requestURI, e);
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorCode", "NULL_POINTER");
        data.put("message", "系统内部错误");
        
        return AjaxResult.error("系统内部错误", data);
    }
    
    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AjaxResult handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}', 非法参数: {}", requestURI, e.getMessage());
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorCode", "ILLEGAL_ARGUMENT");
        data.put("message", e.getMessage());
        
        return AjaxResult.error("参数错误", data);
    }
    
    /**
     * 通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public AjaxResult handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}', 发生系统异常", requestURI, e);
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorCode", "INTERNAL_ERROR");
        data.put("message", "系统繁忙，请稍后再试");
        
        return AjaxResult.error("系统内部错误", data);
    }
}
