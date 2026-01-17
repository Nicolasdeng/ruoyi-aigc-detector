package com.ruoyi.web.exception;

/**
 * 接口限流异常
 * 当用户请求频率超过限制时抛出此异常
 * 
 * @author ruoyi
 */
public class RateLimitException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 限流Key
     */
    private String key;
    
    /**
     * 限流时间窗口（秒）
     */
    private int timeWindow;
    
    /**
     * 限流次数
     */
    private int maxCount;
    
    /**
     * 当前已请求次数
     */
    private int currentCount;
    
    /**
     * 需要等待的秒数（重试时间）
     */
    private long retryAfter;
    
    public RateLimitException(String message) {
        super(message);
    }
    
    public RateLimitException(String message, String key, int timeWindow, int maxCount, int currentCount) {
        super(message);
        this.key = key;
        this.timeWindow = timeWindow;
        this.maxCount = maxCount;
        this.currentCount = currentCount;
    }
    
    public RateLimitException(String message, String key, int timeWindow, int maxCount, int currentCount, long retryAfter) {
        super(message);
        this.key = key;
        this.timeWindow = timeWindow;
        this.maxCount = maxCount;
        this.currentCount = currentCount;
        this.retryAfter = retryAfter;
    }
    
    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public int getTimeWindow() {
        return timeWindow;
    }
    
    public void setTimeWindow(int timeWindow) {
        this.timeWindow = timeWindow;
    }
    
    public int getMaxCount() {
        return maxCount;
    }
    
    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }
    
    public int getCurrentCount() {
        return currentCount;
    }
    
    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
    }
    
    public long getRetryAfter() {
        return retryAfter;
    }
    
    public void setRetryAfter(long retryAfter) {
        this.retryAfter = retryAfter;
    }
}
