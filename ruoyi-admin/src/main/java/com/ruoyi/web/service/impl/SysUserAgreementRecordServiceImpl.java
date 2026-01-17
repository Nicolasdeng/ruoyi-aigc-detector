package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.SysUserAgreement;
import com.ruoyi.web.domain.SysUserAgreementRecord;
import com.ruoyi.web.mapper.SysUserAgreementMapper;
import com.ruoyi.web.mapper.SysUserAgreementRecordMapper;
import com.ruoyi.web.service.ISysUserAgreementRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户协议确认记录服务实现
 * 
 * @author ruoyi
 */
@Service
public class SysUserAgreementRecordServiceImpl implements ISysUserAgreementRecordService {
    
    @Autowired
    private SysUserAgreementRecordMapper recordMapper;
    
    @Autowired
    private SysUserAgreementMapper agreementMapper;
    
    /**
     * 用户确认协议
     * 
     * @param userId 用户ID
     * @param agreementId 协议ID
     * @param ipAddress IP地址
     * @param deviceInfo 设备信息
     * @return 确认记录ID，如果用户已确认则返回null
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long confirmAgreement(Long userId, Long agreementId, String ipAddress, String deviceInfo) {
        // 1. 检查用户是否已确认该协议
        if (hasUserConfirmed(userId, agreementId)) {
            return null;
        }
        
        // 2. 查询协议信息
        SysUserAgreement agreement = agreementMapper.selectByAgreementId(agreementId);
        if (agreement == null) {
            throw new RuntimeException("协议不存在");
        }
        
        // 3. 创建确认记录
        SysUserAgreementRecord record = new SysUserAgreementRecord();
        record.setUserId(userId);
        record.setAgreementId(agreementId);
        record.setAgreementVersion(agreement.getVersion());
        record.setConfirmTime(new Date());
        record.setConfirmIp(ipAddress);
        // 解析设备信息：假设格式为 "deviceType|deviceModel"
        if (deviceInfo != null && deviceInfo.contains("|")) {
            String[] parts = deviceInfo.split("\\|", 2);
            record.setDeviceType(parts[0]);
            if (parts.length > 1) {
                record.setDeviceModel(parts[1]);
            }
        } else {
            record.setDeviceType(deviceInfo);
        }
        
        // 4. 插入记录
        recordMapper.insertRecord(record);
        
        return record.getRecordId();
    }
    
    /**
     * 检查用户是否已确认指定协议
     * 
     * @param userId 用户ID
     * @param agreementId 协议ID
     * @return true=已确认，false=未确认
     */
    @Override
    public boolean hasUserConfirmed(Long userId, Long agreementId) {
        // 获取协议信息以获取版本号
        SysUserAgreement agreement = agreementMapper.selectByAgreementId(agreementId);
        if (agreement == null) {
            return false;
        }
        SysUserAgreementRecord record = recordMapper.checkUserAgreement(userId, agreementId, agreement.getVersion());
        return record != null;
    }
    
    /**
     * 检查用户是否已确认指定类型和版本的协议
     * 
     * @param userId 用户ID
     * @param agreementType 协议类型
     * @param version 协议版本
     * @return true=已确认，false=未确认
     */
    @Override
    public boolean hasUserConfirmedVersion(Long userId, String agreementType, String version) {
        // 将String类型的agreementType转换为Integer
        Integer typeInt = convertAgreementType(agreementType);
        if (typeInt == null) {
            return false;
        }
        
        SysUserAgreementRecord record = recordMapper.selectLatestByUserAndType(userId, typeInt);
        return record != null && version.equals(record.getAgreementVersion());
    }
    
    /**
     * 转换协议类型字符串为Integer
     */
    private Integer convertAgreementType(String agreementType) {
        if (agreementType == null) {
            return null;
        }
        
        // 支持字符串类型名称
        if ("user_agreement".equals(agreementType)) {
            return 1;
        } else if ("privacy_policy".equals(agreementType)) {
            return 2;
        }
        
        // 支持数字字符串
        try {
            return Integer.parseInt(agreementType);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 查询用户的协议确认记录列表
     * 
     * @param userId 用户ID
     * @return 确认记录列表
     */
    @Override
    public List<SysUserAgreementRecord> selectRecordsByUserId(Long userId) {
        SysUserAgreementRecord query = new SysUserAgreementRecord();
        query.setUserId(userId);
        return recordMapper.selectRecordList(query);
    }
    
    /**
     * 查询用户指定类型协议的最新确认记录
     *
     * @param userId 用户ID
     * @param agreementType 协议类型
     * @return 最新确认记录
     */
    @Override
    public SysUserAgreementRecord selectLatestRecordByUserAndType(Long userId, String agreementType) {
        Integer typeInt = convertAgreementType(agreementType);
        if (typeInt == null) {
            return null;
        }
        return recordMapper.selectLatestByUserAndType(userId, typeInt);
    }
    
    /**
     * 查询所有协议确认记录
     * 
     * @param record 查询条件
     * @return 确认记录列表
     */
    @Override
    public List<SysUserAgreementRecord> selectRecordList(SysUserAgreementRecord record) {
        return recordMapper.selectRecordList(record);
    }
    
    /**
     * 根据ID查询确认记录
     * 
     * @param recordId 记录ID
     * @return 确认记录
     */
    @Override
    public SysUserAgreementRecord selectRecordById(Long recordId) {
        return recordMapper.selectByRecordId(recordId);
    }
    
    /**
     * 查询未确认指定协议的用户列表
     * 
     * @param agreementId 协议ID
     * @return 未确认用户ID列表
     */
    /**
     * 查询未确认指定协议的用户ID列表
     * 注意：此方法需要在Mapper中实现，当前暂时返回空列表
     */
    @Override
    public List<Long> selectUnconfirmedUsers(Long agreementId) {
        // TODO: 需要在Mapper中添加selectUnconfirmedUsers方法
        return new ArrayList<>();
    }
    
    /**
     * 统计指定协议的确认人数
     * 
     * @param agreementId 协议ID
     * @return 确认人数
     */
    @Override
    public int countConfirmedUsers(Long agreementId) {
        Integer count = recordMapper.countByAgreementId(agreementId);
        return count != null ? count : 0;
    }
    
    /**
     * 统计指定协议类型的确认人数
     * 
     * @param agreementType 协议类型
     * @return 确认人数
     */
    /**
     * 统计已确认指定类型协议的用户数
     * 注意：此方法需要在Mapper中实现，当前暂时返回0
     */
    @Override
    public int countConfirmedUsersByType(String agreementType) {
        Integer typeInt = convertAgreementType(agreementType);
        if (typeInt == null) {
            return 0;
        }
        
        // TODO: 需要在Mapper中添加countConfirmedUsersByType方法
        // return recordMapper.countConfirmedUsersByType(typeInt);
        return 0;
    }
    
    /**
     * 删除确认记录（仅供管理员使用）
     * 
     * @param recordId 记录ID
     * @return 影响行数
     */
    @Override
    public int deleteRecordById(Long recordId) {
        return recordMapper.deleteRecord(recordId);
    }
    
    /**
     * 批量删除确认记录（仅供管理员使用）
     * 
     * @param recordIds 记录ID数组
     * @return 影响行数
     */
    @Override
    public int deleteRecordByIds(Long[] recordIds) {
        return recordMapper.deleteRecordByIds(recordIds);
    }
}
