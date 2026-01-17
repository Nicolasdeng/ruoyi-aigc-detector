package com.ruoyi.web.service;

import com.ruoyi.web.domain.SysLoginLog;
import java.util.List;

/**
 * 登录日志服务接口
 * 
 * @author ruoyi
 */
public interface ILoginLogService {
    
    /**
     * 记录登录日志
     * 
     * @param log 登录日志信息
     */
    void recordLoginLog(SysLoginLog log);
    
    /**
     * 获取用户的所有登录日志
     * 
     * @param userId 用户ID
     * @return 登录日志列表
     */
    List<SysLoginLog> getLoginLogsByUserId(Long userId);
    
    /**
     * 获取用户最近的N条登录日志
     * 
     * @param userId 用户ID
     * @param limit 返回记录数
     * @return 登录日志列表
     */
    List<SysLoginLog> getRecentLoginLogs(Long userId, Integer limit);
    
    /**
     * 根据ID获取登录日志
     * 
     * @param logId 日志ID
     * @return 登录日志
     */
    SysLoginLog getLoginLogById(Long logId);
    
    /**
     * 统计用户登录次数
     * 
     * @param userId 用户ID
     * @return 登录次数
     */
    Integer countLoginByUserId(Long userId);
    
    /**
     * 删除过期的登录日志
     * 
     * @param days 保留天数
     * @return 删除的记录数
     */
    Integer deleteOldLoginLogs(Integer days);
}
