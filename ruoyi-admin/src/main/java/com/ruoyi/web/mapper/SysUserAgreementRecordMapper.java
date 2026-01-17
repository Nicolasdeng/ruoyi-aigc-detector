package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.SysUserAgreementRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户协议确认记录Mapper接口
 * 
 * @author ruoyi
 */
@Mapper
public interface SysUserAgreementRecordMapper {
    
    /**
     * 查询用户协议确认记录
     * 
     * @param recordId 记录ID
     * @return 用户协议确认记录
     */
    SysUserAgreementRecord selectByRecordId(@Param("recordId") Long recordId);
    
    /**
     * 根据用户ID和协议ID查询最新确认记录
     * 
     * @param userId 用户ID
     * @param agreementId 协议ID
     * @return 用户协议确认记录
     */
    SysUserAgreementRecord selectLatestByUserAndAgreement(@Param("userId") Long userId, 
                                                           @Param("agreementId") Long agreementId);
    
    /**
     * 根据用户ID和协议类型查询最新确认记录
     * 
     * @param userId 用户ID
     * @param agreementType 协议类型 (1=用户协议 2=隐私政策)
     * @return 用户协议确认记录
     */
    SysUserAgreementRecord selectLatestByUserAndType(@Param("userId") Long userId, 
                                                      @Param("agreementType") Integer agreementType);
    
    /**
     * 检查用户是否已确认最新协议
     * 
     * @param userId 用户ID
     * @param agreementId 协议ID
     * @param agreementVersion 协议版本号
     * @return 确认记录（如果已确认）
     */
    SysUserAgreementRecord checkUserAgreement(@Param("userId") Long userId, 
                                              @Param("agreementId") Long agreementId,
                                              @Param("agreementVersion") String agreementVersion);
    
    /**
     * 查询用户的所有协议确认记录
     * 
     * @param userId 用户ID
     * @return 确认记录列表
     */
    List<SysUserAgreementRecord> selectRecordsByUserId(@Param("userId") Long userId);
    
    /**
     * 查询协议确认记录列表
     * 
     * @param record 查询条件
     * @return 确认记录列表
     */
    List<SysUserAgreementRecord> selectRecordList(SysUserAgreementRecord record);
    
    /**
     * 统计协议确认数量
     * 
     * @param agreementId 协议ID
     * @return 确认数量
     */
    int countByAgreementId(@Param("agreementId") Long agreementId);
    
    /**
     * 新增用户协议确认记录
     * 
     * @param record 确认记录
     * @return 影响行数
     */
    int insertRecord(SysUserAgreementRecord record);
    
    /**
     * 批量新增用户协议确认记录
     * 
     * @param records 确认记录列表
     * @return 影响行数
     */
    int batchInsertRecords(@Param("records") List<SysUserAgreementRecord> records);
    
    /**
     * 修改用户协议确认记录
     * 
     * @param record 确认记录
     * @return 影响行数
     */
    int updateRecord(SysUserAgreementRecord record);
    
    /**
     * 删除用户协议确认记录
     * 
     * @param recordId 记录ID
     * @return 影响行数
     */
    int deleteRecord(@Param("recordId") Long recordId);
    
    /**
     * 批量删除用户协议确认记录
     * 
     * @param recordIds 记录ID数组
     * @return 影响行数
     */
    int deleteRecordByIds(@Param("recordIds") Long[] recordIds);
    
    /**
     * 删除用户的所有协议确认记录
     * 
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteRecordsByUserId(@Param("userId") Long userId);
}
