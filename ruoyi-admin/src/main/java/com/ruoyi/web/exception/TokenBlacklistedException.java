package com.ruoyi.web.exception;

/**
 * Token在黑名单中异常
 * 当用户Token已被加入黑名单（如用户退出登录）时抛出此异常
 * 
 * @author ruoyi
 */
public class TokenBlacklistedException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Token值
     */
    private String token;
    
    /**
     * 加入黑名单的原因
     */
    private String reason;
    
    public TokenBlacklistedException(String message) {
        super(message);
    }
    
    public TokenBlacklistedException(String message, String token) {
        super(message);
        this.token = token;
    }
    
    public TokenBlacklistedException(String message, String token, String reason) {
        super(message);
        this.token = token;
        this.reason = reason;
    }
    
    public TokenBlacklistedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}
