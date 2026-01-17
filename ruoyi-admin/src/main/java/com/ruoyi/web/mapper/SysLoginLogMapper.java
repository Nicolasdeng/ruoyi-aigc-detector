package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录日志Mapper接口
 * 
 * @author ruoyi
 */
@Mapper
public interface SysLoginLogMapper {
    
    /**
     * 插入登录日志
     * 
     * @param loginLog 登录日志
     * @return 结果
     */
    int insertLoginLog(SysLoginLog loginLog);
    
    /**
     * 根据用户ID查询登录日志列表
     * 
     * @param userId 用户ID
     * @return 登录日志列表
     */
    List<SysLoginLog> selectLoginLogByUserId(@Param("userId") Long userId);
    
    /**
     * 根据日志ID查询登录日志
     * 
     * @param logId 日志ID
     * @return 登录日志
     */
    SysLoginLog selectLoginLogById(@Param("logId") Long logId);
    
    /**
     * 查询最近N天的登录日志
     * 
     * @param userId 用户ID
     * @param days 天数
     * @return 登录日志列表
     */
    List<SysLoginLog> selectRecentLoginLogs(@Param("userId") Long userId, @Param("days") int days);
    
    /**
     * 统计用户登录次数
     * 
     * @param userId 用户ID
     * @return 登录次数
     */
    int countLoginByUserId(@Param("userId") Long userId);
    
    /**
     * 删除超过指定天数的登录日志
     * 
     * @param days 保留天数
     * @return 删除数量
     */
    int deleteOldLoginLogs(@Param("days") int days);
}
