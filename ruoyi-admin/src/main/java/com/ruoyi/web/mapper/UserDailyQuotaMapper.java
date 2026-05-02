package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.UserDailyQuota;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 用户每日配额Mapper接口
 * 
 * @author ruoyi
 * @date 2026-01-17
 */
public interface UserDailyQuotaMapper {
    
    /**
     * 查询用户每日配额
     * 
     * @param quotaId 用户每日配额主键
     * @return 用户每日配额
     */
    public UserDailyQuota selectUserDailyQuotaByQuotaId(Long quotaId);

    /**
     * 根据用户ID和日期查询配额
     * 
     * @param userId 用户ID
     * @param quotaDate 配额日期
     * @return 用户每日配额
     */
    public UserDailyQuota selectUserDailyQuotaByUserIdAndDate(@Param("userId") Long userId, @Param("quotaDate") Date quotaDate);

    /**
     * 查询用户每日配额列表
     * 
     * @param userDailyQuota 用户每日配额
     * @return 用户每日配额集合
     */
    public List<UserDailyQuota> selectUserDailyQuotaList(UserDailyQuota userDailyQuota);

    /**
     * 新增用户每日配额
     * 
     * @param userDailyQuota 用户每日配额
     * @return 结果
     */
    public int insertUserDailyQuota(UserDailyQuota userDailyQuota);

    /**
     * 修改用户每日配额
     * 
     * @param userDailyQuota 用户每日配额
     * @return 结果
     */
    public int updateUserDailyQuota(UserDailyQuota userDailyQuota);

    /**
     * 删除用户每日配额
     * 
     * @param quotaId 用户每日配额主键
     * @return 结果
     */
    public int deleteUserDailyQuotaByQuotaId(Long quotaId);

    /**
     * 批量删除用户每日配额
     * 
     * @param quotaIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserDailyQuotaByQuotaIds(Long[] quotaIds);

    /**
     * 增加文本检测使用次数
     * 
     * @param userId 用户ID
     * @param quotaDate 配额日期
     * @return 结果
     */
    public int incrementTextUsed(@Param("userId") Long userId, @Param("quotaDate") Date quotaDate);

    /**
     * 增加图片检测使用次数
     * 
     * @param userId 用户ID
     * @param quotaDate 配额日期
     * @return 结果
     */
    public int incrementImageUsed(@Param("userId") Long userId, @Param("quotaDate") Date quotaDate);

    /**
     * 增加视频检测使用次数
     * 
     * @param userId 用户ID
     * @param quotaDate 配额日期
     * @return 结果
     */
    public int incrementVideoUsed(@Param("userId") Long userId, @Param("quotaDate") Date quotaDate);

    /**
     * 增加音频检测使用次数
     * 
     * @param userId 用户ID
     * @param quotaDate 配额日期
     * @return 结果
     */
    public int incrementAudioUsed(@Param("userId") Long userId, @Param("quotaDate") Date quotaDate);

    /**
     * 增加论文检测使用次数
     * 
     * @param userId 用户ID
     * @param quotaDate 配额日期
     * @return 结果
     */
    public int incrementPaperUsed(@Param("userId") Long userId, @Param("quotaDate") Date quotaDate);

    /**
     * 增加看广告获得的文本检测配额
     * 
     * @param userId 用户ID
     * @param quotaDate 配额日期
     * @param bonus 奖励次数
     * @return 结果
     */
    public int incrementTextAdBonus(@Param("userId") Long userId, @Param("quotaDate") Date quotaDate, @Param("bonus") Integer bonus);

    /**
     * 增加看广告获得的图片检测配额
     * 
     * @param userId 用户ID
     * @param quotaDate 配额日期
     * @param bonus 奖励次数
     * @return 结果
     */
    public int incrementImageAdBonus(@Param("userId") Long userId, @Param("quotaDate") Date quotaDate, @Param("bonus") Integer bonus);

    /**
     * 增加看广告获得的视频检测配额
     * 
     * @param userId 用户ID
     * @param quotaDate 配额日期
     * @param bonus 奖励次数
     * @return 结果
     */
    public int incrementVideoAdBonus(@Param("userId") Long userId, @Param("quotaDate") Date quotaDate, @Param("bonus") Integer bonus);

    /**
     * 增加看广告获得的音频检测配额
     * 
     * @param userId 用户ID
     * @param quotaDate 配额日期
     * @param bonus 奖励次数
     * @return 结果
     */
    public int incrementAudioAdBonus(@Param("userId") Long userId, @Param("quotaDate") Date quotaDate, @Param("bonus") Integer bonus);

    /**
     * 增加看广告获得的论文检测配额
     * 
     * @param userId 用户ID
     * @param quotaDate 配额日期
     * @param bonus 奖励次数
     * @return 结果
     */
    public int incrementPaperAdBonus(@Param("userId") Long userId, @Param("quotaDate") Date quotaDate, @Param("bonus") Integer bonus);

    /**
     * 删除指定日期之前的配额记录
     * 
     * @param beforeDate 指定日期
     * @return 结果
     */
    public int deleteQuotaBeforeDate(@Param("beforeDate") Date beforeDate);
}
