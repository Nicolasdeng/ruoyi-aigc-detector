package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.SysLoginLog;
import com.ruoyi.web.mapper.SysLoginLogMapper;
import com.ruoyi.web.service.ILoginLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 登录日志服务实现类
 * 
 * @author ruoyi
 */
@Service
public class LoginLogServiceImpl implements ILoginLogService {
    
    private static final Logger log = LoggerFactory.getLogger(LoginLogServiceImpl.class);
    
    @Autowired
    private SysLoginLogMapper loginLogMapper;
    
    /**
     * 记录登录日志
     */
    @Override
    public void recordLoginLog(SysLoginLog loginLog) {
        try {
            loginLogMapper.insertLoginLog(loginLog);
            log.info("登录日志记录成功，用户ID: {}, 登录IP: {}, 登录结果: {}", 
                loginLog.getUserId(), loginLog.getLoginIp(), loginLog.getLoginResult());
        } catch (Exception e) {
            log.error("记录登录日志失败", e);
            // 日志记录失败不影响登录流程，仅记录错误
        }
    }
    
    /**
     * 获取用户的所有登录日志
     */
    @Override
    public List<SysLoginLog> getLoginLogsByUserId(Long userId) {
        try {
            return loginLogMapper.selectLoginLogByUserId(userId);
        } catch (Exception e) {
            log.error("查询用户登录日志失败，用户ID: {}", userId, e);
            return null;
        }
    }
    
    /**
     * 获取用户最近的N条登录日志
     */
    @Override
    public List<SysLoginLog> getRecentLoginLogs(Long userId, Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 10; // 默认返回最近10条
            }
            return loginLogMapper.selectRecentLoginLogs(userId, limit);
        } catch (Exception e) {
            log.error("查询用户最近登录日志失败，用户ID: {}, 数量: {}", userId, limit, e);
            return null;
        }
    }
    
    /**
     * 根据ID获取登录日志
     */
    @Override
    public SysLoginLog getLoginLogById(Long logId) {
        try {
            return loginLogMapper.selectLoginLogById(logId);
        } catch (Exception e) {
            log.error("查询登录日志失败，日志ID: {}", logId, e);
            return null;
        }
    }
    
    /**
     * 统计用户登录次数
     */
    @Override
    public Integer countLoginByUserId(Long userId) {
        try {
            Integer count = loginLogMapper.countLoginByUserId(userId);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("统计用户登录次数失败，用户ID: {}", userId, e);
            return 0;
        }
    }
    
    /**
     * 删除过期的登录日志
     */
    @Override
    public Integer deleteOldLoginLogs(Integer days) {
        try {
            if (days == null || days <= 0) {
                days = 90; // 默认删除90天前的日志
            }
            int count = loginLogMapper.deleteOldLoginLogs(days);
            log.info("删除{}天前的登录日志，共删除{}条", days, count);
            return count;
        } catch (Exception e) {
            log.error("删除过期登录日志失败，保留天数: {}", days, e);
            return 0;
        }
    }
}
