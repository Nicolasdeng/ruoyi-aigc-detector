package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.UserDailyQuota;
import com.ruoyi.web.domain.UserMembership;
import com.ruoyi.web.mapper.UserDailyQuotaMapper;
import com.ruoyi.web.mapper.UserMembershipMapper;
import com.ruoyi.web.service.IQuotaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 配额服务实现
 */
@Service
public class QuotaServiceImpl implements IQuotaService {
    
    private static final Logger log = LoggerFactory.getLogger(QuotaServiceImpl.class);
    
    // 广告奖励次数限制
    private static final int MAX_AD_BONUS_PER_DAY = 5;
    
    @Autowired
    private UserDailyQuotaMapper quotaMapper;
    
    @Autowired
    private UserMembershipMapper membershipMapper;

    @Override
    public UserDailyQuota getTodayQuota(Long userId) {
        Date today = new Date();
        UserDailyQuota quota = quotaMapper.selectUserDailyQuotaByUserIdAndDate(userId, today);
        if (quota == null) {
            quota = initTodayQuota(userId);
        }
        return quota;
    }

    @Override
    public UserDailyQuota getQuotaByDate(Long userId, Date date) {
        return quotaMapper.selectUserDailyQuotaByUserIdAndDate(userId, date);
    }

    @Override
    @Transactional
    public UserDailyQuota initTodayQuota(Long userId) {
        UserDailyQuota quota = new UserDailyQuota();
        quota.setUserId(userId);
        quota.setQuotaDate(new Date());
        
        // 获取用户会员信息
        UserMembership membership = membershipMapper.selectValidMembershipByUserId(userId);
        
        // 使用isValid()方法判断会员是否有效
        if (membership != null && membership.isValid()) {
            // 会员用户配额
            quota.setTextQuota(100);
            quota.setImageQuota(100);
            quota.setVideoQuota(50);
            quota.setAudioQuota(100);
            quota.setPaperQuota(50);
        } else {
            // 普通用户配额
            quota.setTextQuota(10);
            quota.setImageQuota(10);
            quota.setVideoQuota(5);
            quota.setAudioQuota(10);
            quota.setPaperQuota(5);
        }
        
        // 初始使用量为0
        quota.setTextUsed(0);
        quota.setImageUsed(0);
        quota.setVideoUsed(0);
        quota.setAudioUsed(0);
        quota.setPaperUsed(0);
        
        // 初始广告奖励为0
        quota.setAdBonusQuota(0);
        
        quota.setCreateTime(new Date());
        quota.setUpdateTime(new Date());
        
        quotaMapper.insertUserDailyQuota(quota);
        return quota;
    }

    @Override
    @Transactional
    public boolean consumeTextQuota(Long userId) {
        try {
            getTodayQuota(userId); // 确保配额已初始化
            Date today = new Date();
            int rows = quotaMapper.incrementTextUsed(userId, today);
            return rows > 0;
        } catch (Exception e) {
            log.error("消费文本配额失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean consumeImageQuota(Long userId) {
        try {
            getTodayQuota(userId); // 确保配额已初始化
            Date today = new Date();
            int rows = quotaMapper.incrementImageUsed(userId, today);
            return rows > 0;
        } catch (Exception e) {
            log.error("消费图片配额失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean consumeVideoQuota(Long userId) {
        try {
            getTodayQuota(userId); // 确保配额已初始化
            Date today = new Date();
            int rows = quotaMapper.incrementVideoUsed(userId, today);
            return rows > 0;
        } catch (Exception e) {
            log.error("消费视频配额失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean consumeAudioQuota(Long userId) {
        try {
            getTodayQuota(userId); // 确保配额已初始化
            Date today = new Date();
            int rows = quotaMapper.incrementAudioUsed(userId, today);
            return rows > 0;
        } catch (Exception e) {
            log.error("消费音频配额失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean consumePaperQuota(Long userId) {
        try {
            getTodayQuota(userId); // 确保配额已初始化
            Date today = new Date();
            int rows = quotaMapper.incrementPaperUsed(userId, today);
            return rows > 0;
        } catch (Exception e) {
            log.error("消费论文配额失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public int addAdBonus(Long userId, String detectionType) {
        try {
            // 检查今日广告奖励次数
            if (!canWatchAd(userId, detectionType)) {
                log.warn("用户今日广告奖励次数已达上限: userId={}, type={}", userId, detectionType);
                return 0;
            }
            
            getTodayQuota(userId); // 确保配额已初始化
            Date today = new Date();
            int rows = 0;
            
            switch (detectionType.toLowerCase()) {
                case "text":
                    rows = quotaMapper.incrementTextAdBonus(userId, today, 1);
                    break;
                case "image":
                    rows = quotaMapper.incrementImageAdBonus(userId, today, 1);
                    break;
                case "video":
                    rows = quotaMapper.incrementVideoAdBonus(userId, today, 1);
                    break;
                case "audio":
                    rows = quotaMapper.incrementAudioAdBonus(userId, today, 1);
                    break;
                case "paper":
                    rows = quotaMapper.incrementPaperAdBonus(userId, today, 1);
                    break;
                default:
                    log.warn("不支持的检测类型: {}", detectionType);
                    return 0;
            }
            
            return rows > 0 ? 1 : 0;
        } catch (Exception e) {
            log.error("增加广告奖励失败: userId={}, type={}", userId, detectionType, e);
            return 0;
        }
    }

    @Override
    public boolean canWatchAd(Long userId, String detectionType) {
        try {
            UserDailyQuota quota = getTodayQuota(userId);
            
            // 使用通用的广告奖励配额字段
            int currentAdCount = quota.getAdBonusQuota() != null ? quota.getAdBonusQuota() : 0;
            
            return currentAdCount < MAX_AD_BONUS_PER_DAY;
        } catch (Exception e) {
            log.error("检查广告观看资格失败: userId={}, type={}", userId, detectionType, e);
            return false;
        }
    }

    @Override
    public List<UserDailyQuota> getQuotaUsageStats(Long userId, Date startDate, Date endDate) {
        UserDailyQuota query = new UserDailyQuota();
        query.setUserId(userId);
        // 使用selectUserDailyQuotaList方法，通过日期范围查询
        List<UserDailyQuota> allQuotas = quotaMapper.selectUserDailyQuotaList(query);
        
        // 过滤日期范围
        List<UserDailyQuota> result = new ArrayList<>();
        
        for (UserDailyQuota quota : allQuotas) {
            Date quotaDate = quota.getQuotaDate();
            if (quotaDate != null && !quotaDate.before(startDate) && !quotaDate.after(endDate)) {
                result.add(quota);
            }
        }
        
        return result;
    }

    @Override
    @Transactional
    public int cleanOldQuota(int daysToKeep) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -daysToKeep);
        Date beforeDate = cal.getTime();
        return quotaMapper.deleteQuotaBeforeDate(beforeDate);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public int resetDailyQuota() {
        log.info("开始执行每日配额重置任务");
        int result = cleanOldQuota(30);
        log.info("每日配额重置任务完成,清理了 {} 条历史数据", result);
        return result;
    }

    @Override
    public UserDailyQuota getBaseQuotaByMembershipType(String membershipType) {
        UserDailyQuota baseQuota = new UserDailyQuota();
        
        if ("GOLD".equalsIgnoreCase(membershipType)) {
            // 黄金会员配额
            baseQuota.setTextQuota(100);
            baseQuota.setImageQuota(100);
            baseQuota.setVideoQuota(50);
            baseQuota.setAudioQuota(100);
            baseQuota.setPaperQuota(50);
        } else {
            // 免费用户配额
            baseQuota.setTextQuota(5);
            baseQuota.setImageQuota(5);
            baseQuota.setVideoQuota(3);
            baseQuota.setAudioQuota(5);
            baseQuota.setPaperQuota(3);
        }
        
        return baseQuota;
    }
}
