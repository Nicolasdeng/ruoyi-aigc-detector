package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.SysUserAgreement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户协议Mapper接口
 * 
 * @author ruoyi
 */
@Mapper
public interface SysUserAgreementMapper {
    
    /**
     * 查询用户协议
     * 
     * @param agreementId 协议ID
     * @return 用户协议
     */
    SysUserAgreement selectByAgreementId(@Param("agreementId") Long agreementId);
    
    /**
     * 根据协议类型查询最新的生效协议
     * 
     * @param agreementType 协议类型 (1=用户协议 2=隐私政策)
     * @return 用户协议
     */
    SysUserAgreement selectLatestByType(@Param("agreementType") Integer agreementType);
    
    /**
     * 根据协议类型和版本号查询
     * 
     * @param agreementType 协议类型
     * @param version 版本号
     * @return 用户协议
     */
    SysUserAgreement selectByTypeAndVersion(@Param("agreementType") Integer agreementType, 
                                            @Param("version") String version);
    
    /**
     * 查询用户协议列表
     * 
     * @param agreement 查询条件
     * @return 用户协议列表
     */
    List<SysUserAgreement> selectAgreementList(SysUserAgreement agreement);
    
    /**
     * 查询所有生效的协议
     * 
     * @return 生效协议列表
     */
    List<SysUserAgreement> selectActiveAgreements();
    
    /**
     * 新增用户协议
     * 
     * @param agreement 用户协议
     * @return 影响行数
     */
    int insertAgreement(SysUserAgreement agreement);
    
    /**
     * 修改用户协议
     * 
     * @param agreement 用户协议
     * @return 影响行数
     */
    int updateAgreement(SysUserAgreement agreement);
    
    /**
     * 批量停用旧版本协议
     * 
     * @param agreementType 协议类型
     * @param currentAgreementId 当前协议ID（排除）
     * @return 影响行数
     */
    int deactivateOldVersions(@Param("agreementType") Integer agreementType, 
                              @Param("currentAgreementId") Long currentAgreementId);
    
    /**
     * 删除用户协议
     * 
     * @param agreementId 协议ID
     * @return 影响行数
     */
    int deleteAgreement(@Param("agreementId") Long agreementId);
    
    /**
     * 批量删除用户协议
     * 
     * @param agreementIds 协议ID数组
     * @return 影响行数
     */
    int deleteAgreementByIds(@Param("agreementIds") Long[] agreementIds);
}
