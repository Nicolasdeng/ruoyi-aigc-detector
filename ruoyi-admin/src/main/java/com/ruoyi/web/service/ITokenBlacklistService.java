package com.ruoyi.web.service;

/**
 * Token黑名单服务接口
 * 用于管理注销的Token和强制下线功能
 * 
 * @author ruoyi
 */
public interface ITokenBlacklistService {
    
    /**
     * 将Token加入黑名单
     * 
     * @param token JWT Token
     * @param expireSeconds Token剩余有效期(秒)
     */
    void addToBlacklist(String token, long expireSeconds);
    
    /**
     * 检查Token是否在黑名单中
     * 
     * @param token JWT Token
     * @return true=已加入黑名单(已注销), false=未加入黑名单(有效)
     */
    boolean isBlacklisted(String token);
    
    /**
     * 强制用户下线(将该用户的所有Token加入黑名单)
     * 用于管理员强制用户下线或用户修改密码后使所有设备登录失效
     * 
     * @param userId 用户ID
     * @param expireSeconds Token有效期(秒)
     */
    void addUserTokensToBlacklist(Long userId, long expireSeconds);
    
    /**
     * 清理过期的黑名单记录
     * 此方法由定时任务调用，清理已过期的黑名单记录以节省存储空间
     */
    void cleanExpiredBlacklist();
}
