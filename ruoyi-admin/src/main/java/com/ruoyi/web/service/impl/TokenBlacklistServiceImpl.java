package com.ruoyi.web.service.impl;

import com.ruoyi.web.service.ITokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务实现类
 * 基于Redis实现分布式Token黑名单管理
 * 
 * @author ruoyi
 */
@Service
public class TokenBlacklistServiceImpl implements ITokenBlacklistService {
    
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    
    /** Redis key前缀 */
    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";
    
    /** 用户黑名单key前缀 */
    private static final String USER_BLACKLIST_KEY_PREFIX = "token:user:blacklist:";
    
    /**
     * 将Token加入黑名单
     * 
     * @param token JWT Token
     * @param expireSeconds Token剩余有效期(秒)
     */
    @Override
    public void addToBlacklist(String token, long expireSeconds) {
        if (token == null || token.isEmpty()) {
            return;
        }
        
        String key = BLACKLIST_KEY_PREFIX + token;
        // 将Token存入Redis，过期时间设置为Token的剩余有效期
        // 这样Token过期后，黑名单记录也会自动删除，节省存储空间
        redisTemplate.opsForValue().set(key, "1", expireSeconds, TimeUnit.SECONDS);
    }
    
    /**
     * 检查Token是否在黑名单中
     * 
     * @param token JWT Token
     * @return true=已加入黑名单(已注销), false=未加入黑名单(有效)
     */
    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        String key = BLACKLIST_KEY_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 强制用户下线(将该用户的所有Token加入黑名单)
     * 用于管理员强制用户下线或用户修改密码后使所有设备登录失效
     * 
     * @param userId 用户ID
     * @param expireSeconds Token有效期(秒)
     */
    @Override
    public void addUserTokensToBlacklist(Long userId, long expireSeconds) {
        if (userId == null) {
            return;
        }
        
        // 将用户ID标记为强制下线状态
        String key = USER_BLACKLIST_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, "1", expireSeconds, TimeUnit.SECONDS);
    }
    
    /**
     * 检查用户是否被强制下线
     * 此方法由Token验证拦截器调用
     * 
     * @param userId 用户ID
     * @return true=已被强制下线, false=正常状态
     */
    public boolean isUserBlacklisted(Long userId) {
        if (userId == null) {
            return false;
        }
        
        String key = USER_BLACKLIST_KEY_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 清理过期的黑名单记录
     * 由于使用了Redis的过期机制，黑名单记录会自动过期删除
     * 此方法主要用于手动触发清理或统计信息
     */
    @Override
    public void cleanExpiredBlacklist() {
        // Redis的过期key会自动删除，这里可以添加日志记录或统计信息
        try {
            // 获取所有黑名单key的数量（用于监控）
            Set<Object> tokenKeys = redisTemplate.keys(BLACKLIST_KEY_PREFIX + "*");
            Set<Object> userKeys = redisTemplate.keys(USER_BLACKLIST_KEY_PREFIX + "*");
            
            int tokenCount = tokenKeys != null ? tokenKeys.size() : 0;
            int userCount = userKeys != null ? userKeys.size() : 0;
            
            System.out.println("当前Token黑名单数量: " + tokenCount);
            System.out.println("当前用户强制下线数量: " + userCount);
        } catch (Exception e) {
            System.err.println("清理黑名单记录时发生错误: " + e.getMessage());
        }
    }
}
