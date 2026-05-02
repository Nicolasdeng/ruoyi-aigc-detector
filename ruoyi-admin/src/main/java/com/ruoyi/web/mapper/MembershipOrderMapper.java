package com.ruoyi.web.mapper;

import java.util.Date;
import java.util.List;
import com.ruoyi.web.domain.MembershipOrder;
import org.apache.ibatis.annotations.Param;

/**
 * 会员订单Mapper接口
 * 
 * @author ruoyi
 * @date 2026-01-17
 */
public interface MembershipOrderMapper 
{
    /**
     * 查询会员订单
     * 
     * @param orderId 会员订单主键
     * @return 会员订单
     */
    public MembershipOrder selectMembershipOrderByOrderId(Long orderId);

    /**
     * 根据订单号查询订单
     * 
     * @param orderNo 订单号
     * @return 会员订单
     */
    public MembershipOrder selectMembershipOrderByOrderNo(String orderNo);

    /**
     * 根据微信交易单号查询订单
     * 
     * @param transactionId 微信交易单号
     * @return 会员订单
     */
    public MembershipOrder selectMembershipOrderByTransactionId(String transactionId);

    /**
     * 查询用户的所有订单
     * 
     * @param userId 用户ID
     * @return 订单列表
     */
    public List<MembershipOrder> selectMembershipOrdersByUserId(Long userId);

    /**
     * 查询会员订单列表
     * 
     * @param membershipOrder 会员订单
     * @return 会员订单集合
     */
    public List<MembershipOrder> selectMembershipOrderList(MembershipOrder membershipOrder);

    /**
     * 新增会员订单
     * 
     * @param membershipOrder 会员订单
     * @return 结果
     */
    public int insertMembershipOrder(MembershipOrder membershipOrder);

    /**
     * 修改会员订单
     * 
     * @param membershipOrder 会员订单
     * @return 结果
     */
    public int updateMembershipOrder(MembershipOrder membershipOrder);

    /**
     * 更新订单状态
     * 
     * @param orderNo 订单号
     * @param orderStatus 订单状态
     * @return 结果
     */
    public int updateOrderStatus(@Param("orderNo") String orderNo, @Param("orderStatus") String orderStatus);

    /**
     * 更新订单为已支付状态
     * 
     * @param orderNo 订单号
     * @param transactionId 微信交易单号
     * @param paidTime 支付时间
     * @return 结果
     */
    public int updateOrderToPaid(@Param("orderNo") String orderNo, 
                                  @Param("transactionId") String transactionId,
                                  @Param("paidTime") Date paidTime);

    /**
     * 取消订单
     * 
     * @param orderNo 订单号
     * @return 结果
     */
    public int cancelOrder(@Param("orderNo") String orderNo);

    /**
     * 查询待支付订单（超时未支付需要自动取消）
     * 
     * @param minutes 超时分钟数
     * @return 待支付订单列表
     */
    public List<MembershipOrder> selectPendingOrdersBeforeTime(@Param("minutes") Integer minutes);

    /**
     * 查询用户指定时间范围内的订单总金额
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单总金额
     */
    public java.math.BigDecimal selectTotalAmountByUserIdAndDateRange(@Param("userId") Long userId,
                                                                       @Param("startTime") Date startTime,
                                                                       @Param("endTime") Date endTime);

    /**
     * 查询用户最近一次成功支付的订单
     * 
     * @param userId 用户ID
     * @return 最近订单
     */
    public MembershipOrder selectLatestPaidOrderByUserId(Long userId);

    /**
     * 统计用户订单数量（按状态）
     * 
     * @param userId 用户ID
     * @param orderStatus 订单状态
     * @return 订单数量
     */
    public int countOrdersByUserIdAndStatus(@Param("userId") Long userId, @Param("orderStatus") String orderStatus);

    /**
     * 删除会员订单
     * 
     * @param orderId 会员订单主键
     * @return 结果
     */
    public int deleteMembershipOrderByOrderId(Long orderId);

    /**
     * 批量删除会员订单
     * 
     * @param orderIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMembershipOrderByOrderIds(Long[] orderIds);
}
