package com.ruoyi.web.mapper;

import java.util.Date;
import java.util.List;
import com.ruoyi.web.domain.UserMembership;
import org.apache.ibatis.annotations.Param;

/**
 * 用户会员信息Mapper接口
 * 
 * @author ruoyi
 * @date 2026-01-17
 */
public interface UserMembershipMapper 
{
    /**
     * 查询用户会员信息
     * 
     * @param membershipId 用户会员信息主键
     * @return 用户会员信息
     */
    public UserMembership selectUserMembershipByMembershipId(Long membershipId);

    /**
     * 根据用户ID查询会员信息
     * 
     * @param userId 用户ID
     * @return 用户会员信息
     */
    public UserMembership selectUserMembershipByUserId(Long userId);

    /**
     * 根据用户ID查询有效的会员信息（未过期且激活状态）
     * 
     * @param userId 用户ID
     * @return 有效的用户会员信息
     */
    public UserMembership selectValidMembershipByUserId(Long userId);

    /**
     * 查询用户会员信息列表
     * 
     * @param userMembership 用户会员信息
     * @return 用户会员信息集合
     */
    public List<UserMembership> selectUserMembershipList(UserMembership userMembership);

    /**
     * 新增用户会员信息
     * 
     * @param userMembership 用户会员信息
     * @return 结果
     */
    public int insertUserMembership(UserMembership userMembership);

    /**
     * 修改用户会员信息
     * 
     * @param userMembership 用户会员信息
     * @return 结果
     */
    public int updateUserMembership(UserMembership userMembership);

    /**
     * 更新会员状态
     * 
     * @param userId 用户ID
     * @param isActive 激活状态（1激活 0禁用）
     * @return 结果
     */
    public int updateMembershipStatus(@Param("userId") Long userId, @Param("isActive") Integer isActive);

    /**
     * 更新会员到期时间
     * 
     * @param userId 用户ID
     * @param expireTime 到期时间
     * @return 结果
     */
    public int updateMembershipExpireTime(@Param("userId") Long userId, @Param("expireTime") Date expireTime);

    /**
     * 升级会员等级
     * 
     * @param userId 用户ID
     * @param membershipType 会员类型（GOLD/PLATINUM）
     * @param startTime 开始时间
     * @param expireTime 到期时间
     * @return 结果
     */
    public int upgradeMembership(@Param("userId") Long userId, 
                                 @Param("membershipType") String membershipType,
                                 @Param("startTime") Date startTime,
                                 @Param("expireTime") Date expireTime);

    /**
     * 查询即将过期的会员（用于自动续费提醒）
     * 
     * @param days 多少天内过期
     * @return 即将过期的会员列表
     */
    public List<UserMembership> selectExpiringMemberships(@Param("days") Integer days);

    /**
     * 查询已过期但未处理的会员
     * 
     * @return 已过期的会员列表
     */
    public List<UserMembership> selectExpiredMemberships();

    /**
     * 处理过期会员（设置为不激活状态）
     * 
     * @param membershipId 会员ID
     * @return 结果
     */
    public int deactivateExpiredMembership(Long membershipId);

    /**
     * 累计会员总消费金额
     * 
     * @param userId 用户ID
     * @param amount 金额
     * @return 结果
     */
    public int incrementTotalSpent(@Param("userId") Long userId, @Param("amount") java.math.BigDecimal amount);

    /**
     * 切换自动续费状态
     * 
     * @param userId 用户ID
     * @param autoRenew 自动续费状态（1开启 0关闭）
     * @return 结果
     */
    public int updateAutoRenewStatus(@Param("userId") Long userId, @Param("autoRenew") Integer autoRenew);

    /**
     * 删除用户会员信息
     * 
     * @param membershipId 用户会员信息主键
     * @return 结果
     */
    public int deleteUserMembershipByMembershipId(Long membershipId);

    /**
     * 批量删除用户会员信息
     * 
     * @param membershipIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserMembershipByMembershipIds(Long[] membershipIds);
}
