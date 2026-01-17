package com.ruoyi.web.exception;

/**
 * 用户未登录异常
 * 当用户未登录或Token无效时抛出此异常
 * 
 * @author ruoyi
 */
public class UnauthorizedException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Token值
     */
    private String token;
    
    /**
     * 请求的URI
     */
    private String requestUri;
    
    /**
     * 认证失败的原因
     */
    private String reason;
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public UnauthorizedException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param token Token值
     * @param requestUri 请求的URI
     * @param reason 认证失败的原因
     */
    public UnauthorizedException(String message, String token, String requestUri, String reason) {
        super(message);
        this.token = token;
        this.requestUri = requestUri;
        this.reason = reason;
    }
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 原始异常
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param token Token值
     * @param requestUri 请求的URI
     * @param reason 认证失败的原因
     * @param cause 原始异常
     */
    public UnauthorizedException(String message, String token, String requestUri, String reason, Throwable cause) {
        super(message, cause);
        this.token = token;
        this.requestUri = requestUri;
        this.reason = reason;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getRequestUri() {
        return requestUri;
    }
    
    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}
