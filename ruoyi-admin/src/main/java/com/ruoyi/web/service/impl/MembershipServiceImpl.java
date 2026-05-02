package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.UserMembership;
import com.ruoyi.web.mapper.UserMembershipMapper;
import com.ruoyi.web.service.IMembershipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 会员服务实现类
 * 
 * @author ruoyi
 */
@Service
public class MembershipServiceImpl implements IMembershipService {
    
    private static final Logger log = LoggerFactory.getLogger(MembershipServiceImpl.class);
    
    @Autowired
    private UserMembershipMapper userMembershipMapper;
    
    /**
     * 查询用户当前有效会员信息
     */
    @Override
    public UserMembership getCurrentMembership(Long userId) {
        if (userId == null) {
            log.warn("获取会员信息失败: 用户ID为空");
            return null;
        }
        
        try {
            // 使用正确的Mapper方法：selectValidMembershipByUserId
            UserMembership membership = userMembershipMapper.selectValidMembershipByUserId(userId);
            if (membership != null) {
                log.info("成功获取用户{}的会员信息", userId);
            }
            return membership;
        } catch (Exception e) {
            log.error("获取用户{}的会员信息时发生错误", userId, e);
            return null;
        }
    }
    
    /**
     * 检查用户是否为付费会员
     */
    @Override
    public boolean isPaidMember(Long userId) {
        if (userId == null) {
            return false;
        }
        
        try {
            // 使用selectValidMembershipByUserId查询有效会员
            UserMembership membership = userMembershipMapper.selectValidMembershipByUserId(userId);
            
            // selectValidMembershipByUserId已经确保会员未过期且激活，只需检查会员类型
            if (membership == null) {
                return false;
            }
            
            // 检查是否为付费会员（非FREE）
            return !"FREE".equals(membership.getMembershipType());
        } catch (Exception e) {
            log.error("检查用户{}会员状态时发生错误", userId, e);
            return false;
        }
    }
    
    /**
     * 获取用户会员等级
     */
    @Override
    public String getMembershipType(Long userId) {
        if (userId == null) {
            log.warn("获取会员等级失败: 用户ID为空");
            return "FREE";
        }
        
        try {
            // 使用selectValidMembershipByUserId查询有效会员
            UserMembership membership = userMembershipMapper.selectValidMembershipByUserId(userId);
            
            if (membership == null) {
                return "FREE";
            }
            
            return membership.getMembershipType() != null ? membership.getMembershipType() : "FREE";
        } catch (Exception e) {
            log.error("获取用户{}会员等级时发生错误", userId, e);
            return "FREE";
        }
    }
    
    /**
     * 开通会员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean activateMembership(Long userId, String packageType, Long orderId) {
        if (userId == null || packageType == null) {
            log.warn("激活会员失败: 用户ID或套餐类型为空");
            return false;
        }
        
        try {
            // 使用selectUserMembershipByUserId查询用户会员记录（包括已过期的）
            UserMembership membership = userMembershipMapper.selectUserMembershipByUserId(userId);
            
            // 计算过期时间
            Date expireTime = calculateExpireTime(packageType);
            
            // 如果用户还没有会员记录，创建新记录
            if (membership == null) {
                membership = new UserMembership();
                membership.setUserId(userId);
                membership.setMembershipType("GOLD");
                membership.setIsActive(1); // 使用isActive字段
                membership.setCreateTime(new Date());
                membership.setExpireTime(expireTime);
                
                // 使用insertUserMembership方法
                int result = userMembershipMapper.insertUserMembership(membership);
                if (result > 0) {
                    log.info("成功为用户{}创建会员记录，套餐类型: {}", userId, packageType);
                    return true;
                }
            } else {
                // 更新现有会员记录 - 使用upgradeMembership方法
                Date startTime = new Date();
                int result = userMembershipMapper.upgradeMembership(
                    userId, 
                    "GOLD", 
                    startTime, 
                    expireTime
                );
                
                if (result > 0) {
                    log.info("成功更新用户{}的会员状态，套餐类型: {}", userId, packageType);
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            log.error("激活用户{}会员时发生错误", userId, e);
            throw new RuntimeException("激活会员失败", e);
        }
    }
    
    /**
     * 续费会员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean renewMembership(Long userId, String packageType, Long orderId) {
        if (userId == null || packageType == null) {
            log.warn("续费会员失败: 用户ID或套餐类型为空");
            return false;
        }
        
        try {
            // 使用selectUserMembershipByUserId查询用户会员记录
            UserMembership membership = userMembershipMapper.selectUserMembershipByUserId(userId);
            
            if (membership == null) {
                log.warn("续费失败: 用户{}没有会员记录", userId);
                return activateMembership(userId, packageType, orderId);
            }
            
            // 计算新的过期时间（从当前过期时间延长，如果已过期则从现在开始）
            Date currentExpireTime = membership.getExpireTime();
            Date now = new Date();
            Date baseTime = (currentExpireTime != null && currentExpireTime.after(now)) 
                ? currentExpireTime 
                : now;
            
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(baseTime);
            
            if ("WEEK".equals(packageType)) {
                calendar.add(Calendar.DAY_OF_MONTH, 7);
            } else if ("MONTH".equals(packageType)) {
                calendar.add(Calendar.MONTH, 1);
            }
            
            Date newExpireTime = calendar.getTime();
            
            // 使用updateMembershipExpireTime方法，参数是userId而不是membershipId
            int result = userMembershipMapper.updateMembershipExpireTime(
                userId, 
                newExpireTime
            );
            
            if (result > 0) {
                log.info("成功为用户{}续费会员，套餐类型: {}", userId, packageType);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("续费用户{}会员时发生错误", userId, e);
            throw new RuntimeException("续费会员失败", e);
        }
    }
    
    /**
     * 定时任务：处理过期会员
     * 每天凌晨2点执行
     */
    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public int handleExpiredMemberships() {
        log.info("开始处理过期会员...");
        
        try {
            // 使用selectExpiredMemberships查询已过期的会员
            List<UserMembership> expiredMemberships = userMembershipMapper.selectExpiredMemberships();
            int count = 0;
            
            for (UserMembership membership : expiredMemberships) {
                // 使用deactivateExpiredMembership停用过期会员，使用getId()方法
                int result = userMembershipMapper.deactivateExpiredMembership(membership.getId());
                if (result > 0) {
                    count++;
                }
            }
            
            log.info("处理过期会员完成，共处理{}条记录", count);
            return count;
        } catch (Exception e) {
            log.error("处理过期会员时发生错误", e);
            return 0;
        }
    }
    
    /**
     * 获取即将过期的会员列表
     */
    @Override
    public List<UserMembership> getExpiringMemberships(int days) {
        if (days < 0) {
            log.warn("获取即将过期会员失败: 天数不能为负数");
            return new ArrayList<>();
        }
        
        try {
            // 使用selectExpiringMemberships方法，参数为Integer类型
            return userMembershipMapper.selectExpiringMemberships(Integer.valueOf(days));
        } catch (Exception e) {
            log.error("获取即将过期会员列表时发生错误", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 增加用户累计消费金额
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementTotalSpent(Long userId, BigDecimal amount) {
        if (userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("更新消费金额失败: 参数无效");
            return false;
        }
        
        try {
            // 直接使用incrementTotalSpent方法，参数是userId和amount
            int result = userMembershipMapper.incrementTotalSpent(userId, amount);
            
            if (result > 0) {
                log.info("成功更新用户{}的消费金额: {}", userId, amount);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("更新用户{}消费金额时发生错误", userId, e);
            throw new RuntimeException("更新消费金额失败", e);
        }
    }
    
    /**
     * 查询会员统计信息
     */
    @Override
    public Map<String, Object> getMembershipStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 使用selectUserMembershipList查询所有会员
            UserMembership query = new UserMembership();
            List<UserMembership> allMemberships = userMembershipMapper.selectUserMembershipList(query);
            
            int freeCount = 0;
            int goldCount = 0;
            int activeCount = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;
            
            for (UserMembership membership : allMemberships) {
                if ("FREE".equals(membership.getMembershipType())) {
                    freeCount++;
                } else if ("GOLD".equals(membership.getMembershipType())) {
                    goldCount++;
                }
                
                // 使用isActive字段（1表示激活）
                if (membership.getIsActive() != null && membership.getIsActive() == 1) {
                    activeCount++;
                }
            }
            
            statistics.put("totalMembers", allMemberships.size());
            statistics.put("freeMembers", freeCount);
            statistics.put("goldMembers", goldCount);
            statistics.put("activeMembers", activeCount);
            statistics.put("totalRevenue", totalRevenue);
            
            log.info("成功获取会员统计信息");
            return statistics;
        } catch (Exception e) {
            log.error("获取会员统计信息时发生错误", e);
            return statistics;
        }
    }
    
    /**
     * 查询用户会员历史记录
     */
    @Override
    public List<UserMembership> getMembershipHistory(Long userId) {
        if (userId == null) {
            log.warn("获取会员历史失败: 用户ID为空");
            return new ArrayList<>();
        }
        
        try {
            // 使用selectUserMembershipByUserId查询用户会员记录
            // 注意：该方法返回单个记录，如果需要历史记录列表，可能需要调整逻辑
            UserMembership membership = userMembershipMapper.selectUserMembershipByUserId(userId);
            List<UserMembership> history = new ArrayList<>();
            if (membership != null) {
                history.add(membership);
            }
            log.info("成功获取用户{}的会员历史记录，共{}条", userId, history.size());
            return history;
        } catch (Exception e) {
            log.error("获取用户{}会员历史记录时发生错误", userId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据会员类型获取会员权益说明
     */
    @Override
    public Map<String, Object> getMembershipBenefits(String membershipType) {
        Map<String, Object> benefits = new HashMap<>();
        
        if ("FREE".equals(membershipType)) {
            benefits.put("type", "FREE");
            benefits.put("name", "免费会员");
            benefits.put("dailyTextQuota", 3);
            benefits.put("dailyImageQuota", 2);
            benefits.put("dailyVideoQuota", 1);
            benefits.put("dailyAudioQuota", 1);
            benefits.put("dailyPaperQuota", 1);
            benefits.put("features", Arrays.asList(
                "基础AI检测功能",
                "每日3次文本检测",
                "每日2次图片检测",
                "每日1次视频检测",
                "每日1次音频检测",
                "每日1次论文检测"
            ));
        } else if ("GOLD".equals(membershipType)) {
            benefits.put("type", "GOLD");
            benefits.put("name", "黄金会员");
            benefits.put("dailyTextQuota", 50);
            benefits.put("dailyImageQuota", 30);
            benefits.put("dailyVideoQuota", 20);
            benefits.put("dailyAudioQuota", 20);
            benefits.put("dailyPaperQuota", 10);
            benefits.put("features", Arrays.asList(
                "全部高级AI检测功能",
                "每日50次文本检测",
                "每日30次图片检测",
                "每日20次视频检测",
                "每日20次音频检测",
                "每日10次论文检测",
                "优先处理队列",
                "详细检测报告",
                "历史记录保存"
            ));
        } else {
            benefits.put("type", "UNKNOWN");
            benefits.put("name", "未知会员类型");
        }
        
        return benefits;
    }
    
    /**
     * 获取套餐原价
     */
    @Override
    public BigDecimal getPackageOriginalPrice(String packageType) {
        if ("WEEK".equals(packageType)) {
            return new BigDecimal("29.90");
        } else if ("MONTH".equals(packageType)) {
            return new BigDecimal("99.90");
        } else {
            log.warn("未知的套餐类型: {}", packageType);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 获取套餐现价
     */
    @Override
    public BigDecimal getPackagePrice(String packageType) {
        if ("WEEK".equals(packageType)) {
            return new BigDecimal("19.90");
        } else if ("MONTH".equals(packageType)) {
            return new BigDecimal("69.90");
        } else {
            log.warn("未知的套餐类型: {}", packageType);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 计算会员过期时间
     */
    private Date calculateExpireTime(String packageType) {
        Calendar calendar = Calendar.getInstance();
        
        if ("WEEK".equals(packageType)) {
            calendar.add(Calendar.DAY_OF_MONTH, 7);
        } else if ("MONTH".equals(packageType)) {
            calendar.add(Calendar.MONTH, 1);
        } else {
            // 默认一个月
            calendar.add(Calendar.MONTH, 1);
        }
        
        return calendar.getTime();
    }
}
