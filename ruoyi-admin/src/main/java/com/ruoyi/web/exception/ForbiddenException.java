package com.ruoyi.web.exception;

/**
 * 权限不足异常
 * 当用户无权限访问特定资源时抛出此异常
 * 
 * @author ruoyi
 */
public class ForbiddenException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 请求的URI
     */
    private String requestUri;
    
    /**
     * 所需权限
     */
    private String requiredPermission;
    
    /**
     * 用户当前权限
     */
    private String currentPermission;
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public ForbiddenException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param userId 用户ID
     * @param requestUri 请求的URI
     * @param requiredPermission 所需权限
     */
    public ForbiddenException(String message, Long userId, String requestUri, String requiredPermission) {
        super(message);
        this.userId = userId;
        this.requestUri = requestUri;
        this.requiredPermission = requiredPermission;
    }
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param userId 用户ID
     * @param requestUri 请求的URI
     * @param requiredPermission 所需权限
     * @param currentPermission 用户当前权限
     */
    public ForbiddenException(String message, Long userId, String requestUri, String requiredPermission, String currentPermission) {
        super(message);
        this.userId = userId;
        this.requestUri = requestUri;
        this.requiredPermission = requiredPermission;
        this.currentPermission = currentPermission;
    }
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 原始异常
     */
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getRequestUri() {
        return requestUri;
    }
    
    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }
    
    public String getRequiredPermission() {
        return requiredPermission;
    }
    
    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }
    
    public String getCurrentPermission() {
        return currentPermission;
    }
    
    public void setCurrentPermission(String currentPermission) {
        this.currentPermission = currentPermission;
    }
}
