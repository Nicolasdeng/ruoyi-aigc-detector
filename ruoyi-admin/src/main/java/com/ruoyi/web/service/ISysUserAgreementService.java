package com.ruoyi.web.service;

import com.ruoyi.web.domain.SysUserAgreement;
import java.util.List;

/**
 * 用户协议服务接口
 * 
 * @author ruoyi
 */
public interface ISysUserAgreementService {
    
    /**
     * 根据协议类型获取最新生效的协议
     * 
     * @param agreementType 协议类型（1-用户协议/2-隐私政策）
     * @return 协议信息，如果不存在返回null
     */
    SysUserAgreement getLatestAgreementByType(Integer agreementType);
    
    /**
     * 根据ID查询协议
     * 
     * @param agreementId 协议ID
     * @return 协议信息
     */
    SysUserAgreement getAgreementById(Long agreementId);
    
    /**
     * 查询协议列表
     * 
     * @param agreement 查询条件
     * @return 协议列表
     */
    List<SysUserAgreement> selectAgreementList(SysUserAgreement agreement);
    
    /**
     * 新增协议
     * 
     * @param agreement 协议信息
     * @return 结果
     */
    int insertAgreement(SysUserAgreement agreement);
    
    /**
     * 修改协议
     * 
     * @param agreement 协议信息
     * @return 结果
     */
    int updateAgreement(SysUserAgreement agreement);
    
    /**
     * 删除协议
     * 
     * @param agreementId 协议ID
     * @return 结果
     */
    int deleteAgreementById(Long agreementId);
    
    /**
     * 发布新版本协议（自动停用旧版本）
     * 
     * @param agreement 新版本协议信息
     * @return 结果
     */
    int publishNewVersion(SysUserAgreement agreement);
    
    /**
     * 停用指定类型的所有旧版本协议
     * 
     * @param agreementType 协议类型
     * @param excludeAgreementId 排除的协议ID（新版本ID）
     * @return 结果
     */
    int deactivateOldVersions(Integer agreementType, Long excludeAgreementId);
    
    /**
     * 检查用户是否需要确认新版本协议
     * 
     * @param userId 用户ID
     * @param agreementType 协议类型（1-用户协议/2-隐私政策）
     * @return true-需要确认，false-不需要确认
     */
    boolean needsUserConfirmation(Long userId, Integer agreementType);
}
