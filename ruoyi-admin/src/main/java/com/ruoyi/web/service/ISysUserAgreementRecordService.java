package com.ruoyi.web.service;

import com.ruoyi.web.domain.SysUserAgreementRecord;
import java.util.List;

/**
 * 用户协议确认记录服务接口
 * 
 * @author ruoyi
 */
public interface ISysUserAgreementRecordService {
    
    /**
     * 用户确认协议
     * 
     * @param userId 用户ID
     * @param agreementId 协议ID
     * @param ipAddress IP地址
     * @param deviceInfo 设备信息
     * @return 确认记录ID，如果用户已确认则返回null
     */
    Long confirmAgreement(Long userId, Long agreementId, String ipAddress, String deviceInfo);
    
    /**
     * 检查用户是否已确认指定协议
     * 
     * @param userId 用户ID
     * @param agreementId 协议ID
     * @return true=已确认，false=未确认
     */
    boolean hasUserConfirmed(Long userId, Long agreementId);
    
    /**
     * 检查用户是否已确认指定类型和版本的协议
     * 
     * @param userId 用户ID
     * @param agreementType 协议类型
     * @param version 协议版本
     * @return true=已确认，false=未确认
     */
    boolean hasUserConfirmedVersion(Long userId, String agreementType, String version);
    
    /**
     * 查询用户的协议确认记录列表
     * 
     * @param userId 用户ID
     * @return 确认记录列表
     */
    List<SysUserAgreementRecord> selectRecordsByUserId(Long userId);
    
    /**
     * 查询用户指定类型协议的最新确认记录
     * 
     * @param userId 用户ID
     * @param agreementType 协议类型
     * @return 最新确认记录
     */
    SysUserAgreementRecord selectLatestRecordByUserAndType(Long userId, String agreementType);
    
    /**
     * 查询所有协议确认记录
     * 
     * @param record 查询条件
     * @return 确认记录列表
     */
    List<SysUserAgreementRecord> selectRecordList(SysUserAgreementRecord record);
    
    /**
     * 根据ID查询确认记录
     * 
     * @param recordId 记录ID
     * @return 确认记录
     */
    SysUserAgreementRecord selectRecordById(Long recordId);
    
    /**
     * 查询未确认指定协议的用户列表
     * 
     * @param agreementId 协议ID
     * @return 未确认用户ID列表
     */
    List<Long> selectUnconfirmedUsers(Long agreementId);
    
    /**
     * 统计指定协议的确认人数
     * 
     * @param agreementId 协议ID
     * @return 确认人数
     */
    int countConfirmedUsers(Long agreementId);
    
    /**
     * 统计指定协议类型的确认人数
     * 
     * @param agreementType 协议类型
     * @return 确认人数
     */
    int countConfirmedUsersByType(String agreementType);
    
    /**
     * 删除确认记录（仅供管理员使用）
     * 
     * @param recordId 记录ID
     * @return 影响行数
     */
    int deleteRecordById(Long recordId);
    
    /**
     * 批量删除确认记录（仅供管理员使用）
     * 
     * @param recordIds 记录ID数组
     * @return 影响行数
     */
    int deleteRecordByIds(Long[] recordIds);
}
