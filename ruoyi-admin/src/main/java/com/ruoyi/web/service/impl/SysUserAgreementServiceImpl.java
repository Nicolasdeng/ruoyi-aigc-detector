package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.SysUserAgreement;
import com.ruoyi.web.domain.SysUserAgreementRecord;
import com.ruoyi.web.mapper.SysUserAgreementMapper;
import com.ruoyi.web.mapper.SysUserAgreementRecordMapper;
import com.ruoyi.web.service.ISysUserAgreementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

/**
 * 用户协议服务实现
 * 
 * @author ruoyi
 */
@Service
public class SysUserAgreementServiceImpl implements ISysUserAgreementService {
    
    @Autowired
    private SysUserAgreementMapper agreementMapper;
    
    @Autowired
    private SysUserAgreementRecordMapper agreementRecordMapper;
    
    /**
     * 根据协议类型获取最新生效的协议
     */
    @Override
    public SysUserAgreement getLatestAgreementByType(Integer agreementType) {
        return agreementMapper.selectLatestByType(agreementType);
    }
    
    /**
     * 根据ID查询协议
     */
    @Override
    public SysUserAgreement getAgreementById(Long agreementId) {
        return agreementMapper.selectByAgreementId(agreementId);
    }
    
    /**
     * 查询协议列表
     */
    @Override
    public List<SysUserAgreement> selectAgreementList(SysUserAgreement agreement) {
        return agreementMapper.selectAgreementList(agreement);
    }
    
    /**
     * 新增协议
     */
    @Override
    public int insertAgreement(SysUserAgreement agreement) {
        agreement.setCreateTime(new Date());
        return agreementMapper.insertAgreement(agreement);
    }
    
    /**
     * 修改协议
     */
    @Override
    public int updateAgreement(SysUserAgreement agreement) {
        agreement.setUpdateTime(new Date());
        return agreementMapper.updateAgreement(agreement);
    }
    
    /**
     * 删除协议
     */
    @Override
    public int deleteAgreementById(Long agreementId) {
        return agreementMapper.deleteAgreement(agreementId);
    }
    
    /**
     * 发布新版本协议（自动停用旧版本）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int publishNewVersion(SysUserAgreement agreement) {
        // 1. 停用旧版本
        deactivateOldVersions(agreement.getAgreementType(), null);
        
        // 2. 设置新版本属性
        agreement.setIsActive(1);
        agreement.setCreateTime(new Date());
        
        // 3. 插入新版本
        return agreementMapper.insertAgreement(agreement);
    }
    
    /**
     * 停用指定类型的所有旧版本协议
     */
    @Override
    public int deactivateOldVersions(Integer agreementType, Long excludeAgreementId) {
        return agreementMapper.deactivateOldVersions(agreementType, excludeAgreementId);
    }
    
    /**
     * 检查用户是否需要确认新版本协议
     */
    @Override
    public boolean needsUserConfirmation(Long userId, Integer agreementType) {
        // 1. 获取最新生效的协议
        SysUserAgreement latestAgreement = getLatestAgreementByType(agreementType);
        if (latestAgreement == null) {
            // 如果没有协议，不需要确认
            return false;
        }
        
        // 2. 获取用户最新确认记录
        SysUserAgreementRecord latestRecord = agreementRecordMapper
            .selectLatestByUserAndType(userId, agreementType);
        
        if (latestRecord == null) {
            // 用户从未确认过此类型协议，需要确认
            return true;
        }
        
        // 3. 比较版本号
        return !latestAgreement.getVersion().equals(latestRecord.getAgreementVersion());
    }
}
