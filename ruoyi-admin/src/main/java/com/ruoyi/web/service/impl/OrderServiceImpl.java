package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.MembershipOrder;
import com.ruoyi.web.mapper.MembershipOrderMapper;
import com.ruoyi.web.service.IMembershipService;
import com.ruoyi.web.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 会员订单服务实现类
 * 
 * @author ruoyi
 */
@Service
public class OrderServiceImpl implements IOrderService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    @Autowired
    private MembershipOrderMapper orderMapper;
    
    @Autowired
    private IMembershipService membershipService;
    
    /**
     * 创建会员订单（基于套餐类型）
     */
    @Override
    @Transactional
    public MembershipOrder createOrder(Long userId, String packageType) {
        try {
            // 检查是否有待支付订单
            if (hasPendingOrder(userId)) {
                log.warn("用户{}已有待支付订单，请先完成或取消现有订单", userId);
                throw new RuntimeException("您有未完成的订单，请先完成或取消现有订单");
            }
            
            // 验证套餐类型
            if (!"WEEK".equals(packageType) && !"MONTH".equals(packageType)) {
                throw new RuntimeException("无效的套餐类型：" + packageType);
            }
            
            // 获取固定价格
            BigDecimal originalPrice = getPackageOriginalPrice(packageType);
            BigDecimal finalPrice = getPackagePrice(packageType);
            
            // 创建订单对象
            MembershipOrder order = new MembershipOrder();
            order.setUserId(userId);
            order.setOrderNo(generateOrderNo());
            order.setPackageType(packageType);
            order.setOriginalPrice(originalPrice);
            order.setFinalPrice(finalPrice);
            order.setOrderStatus("PENDING");
            order.setCreateTime(new Date());
            
            // 插入订单
            int rows = orderMapper.insertMembershipOrder(order);
            if (rows > 0) {
                log.info("创建会员订单成功，订单号：{}，用户：{}，套餐类型：{}，原价：{}，实付：{}", 
                    order.getOrderNo(), userId, packageType, originalPrice, finalPrice);
                return order;
            }
            
            throw new RuntimeException("创建订单失败");
        } catch (Exception e) {
            log.error("创建订单失败", e);
            throw new RuntimeException("创建订单失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据订单号查询订单
     */
    @Override
    public MembershipOrder getOrderByOrderNo(String orderNo) {
        return orderMapper.selectMembershipOrderByOrderNo(orderNo);
    }
    
    /**
     * 根据订单ID查询订单
     */
    @Override
    public MembershipOrder getOrderById(Long orderId) {
        return orderMapper.selectMembershipOrderByOrderId(orderId);
    }
    
    /**
     * 查询用户的所有订单
     */
    @Override
    public List<MembershipOrder> getUserOrders(Long userId) {
        return orderMapper.selectMembershipOrdersByUserId(userId);
    }
    
    /**
     * 查询用户指定状态的订单
     */
    @Override
    public List<MembershipOrder> getUserOrdersByStatus(Long userId, String orderStatus) {
        MembershipOrder query = new MembershipOrder();
        query.setUserId(userId);
        query.setOrderStatus(orderStatus);
        return orderMapper.selectMembershipOrderList(query);
    }
    
    /**
     * 支付订单
     */
    @Override
    @Transactional
    public boolean payOrder(String orderNo, String transactionId) {
        try {
            // 查询订单
            MembershipOrder order = orderMapper.selectMembershipOrderByOrderNo(orderNo);
            if (order == null) {
                log.warn("订单不存在：{}", orderNo);
                return false;
            }
            
            // 检查订单状态
            if (!order.isPending()) {
                log.warn("订单状态不正确，无法支付：{}，当前状态：{}", orderNo, order.getOrderStatus());
                return false;
            }
            
            // 更新订单状态为已支付
            int rows = orderMapper.updateOrderToPaid(orderNo, transactionId, new Date());
            if (rows == 0) {
                log.warn("更新订单状态失败：{}", orderNo);
                return false;
            }
            
            // 激活或续费会员
            boolean membershipSuccess = membershipService.activateMembership(
                order.getUserId(), 
                order.getPackageType(),
                order.getId()
            );
            
            if (!membershipSuccess) {
                log.error("开通会员失败，订单：{}", orderNo);
                throw new RuntimeException("开通会员失败");
            }
            
            // 增加累计消费
            membershipService.incrementTotalSpent(order.getUserId(), order.getFinalPrice());
            
            log.info("订单支付成功：{}，微信交易号：{}，用户：{}，套餐类型：{}", 
                orderNo, transactionId, order.getUserId(), order.getPackageType());
            
            return true;
        } catch (Exception e) {
            log.error("支付订单失败", e);
            throw new RuntimeException("支付订单失败：" + e.getMessage());
        }
    }
    
    /**
     * 取消订单（通过订单号和用户ID）
     */
    @Override
    @Transactional
    public boolean cancelOrder(String orderNo, Long userId) {
        try {
            // 通过订单号查询订单
            MembershipOrder order = orderMapper.selectMembershipOrderByOrderNo(orderNo);
            if (order == null) {
                log.warn("订单不存在：{}", orderNo);
                return false;
            }
            
            // 验证订单归属
            if (!order.getUserId().equals(userId)) {
                log.warn("订单{}不属于用户{}，无权操作", orderNo, userId);
                return false;
            }
            
            // 只有待支付状态才能取消
            if (!order.isPending()) {
                log.warn("订单状态不正确，无法取消：{}，当前状态：{}", orderNo, order.getOrderStatus());
                return false;
            }
            
            // 更新订单状态为已取消
            int rows = orderMapper.cancelOrder(orderNo);
            if (rows > 0) {
                log.info("取消订单成功：{}，用户：{}", orderNo, userId);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("取消订单失败", e);
            throw new RuntimeException("取消订单失败：" + e.getMessage());
        }
    }
    
    /**
     * 申请退款（通过订单号、用户ID和退款原因）
     */
    @Override
    @Transactional
    public boolean requestRefund(String orderNo, Long userId, String refundReason) {
        try {
            // 通过订单号查询订单
            MembershipOrder order = orderMapper.selectMembershipOrderByOrderNo(orderNo);
            if (order == null) {
                log.warn("订单不存在：{}", orderNo);
                return false;
            }
            
            // 验证订单归属
            if (!order.getUserId().equals(userId)) {
                log.warn("订单{}不属于用户{}，无权操作", orderNo, userId);
                return false;
            }
            
            // 只有已支付状态才能申请退款
            if (!order.isPaid()) {
                log.warn("订单状态不正确，无法申请退款：{}，当前状态：{}", orderNo, order.getOrderStatus());
                return false;
            }
            
            // 检查是否在退款期限内（支付后7天内可退款）
            Date paidTime = order.getPaidTime();
            Date now = new Date();
            long diffInMillis = now.getTime() - paidTime.getTime();
            long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
            if (diffInDays > 7) {
                log.warn("订单已超过退款期限：{}，支付时间：{}", orderNo, paidTime);
                throw new RuntimeException("该订单已超过退款期限（支付后7天内可退款）");
            }
            
            // 更新订单状态为退款中
            order.setOrderStatus("REFUNDING");
            order.setUpdateTime(now);
            int rows = orderMapper.updateMembershipOrder(order);
            
            if (rows > 0) {
                log.info("申请退款成功，订单：{}，用户：{}，退款原因：{}", orderNo, userId, refundReason);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("申请退款失败", e);
            throw new RuntimeException("申请退款失败：" + e.getMessage());
        }
    }
    
    /**
     * 处理退款（管理员操作，通过订单号和退款金额）
     */
    @Override
    @Transactional
    public boolean processRefund(String orderNo, BigDecimal refundAmount) {
        try {
            // 通过订单号查询订单
            MembershipOrder order = orderMapper.selectMembershipOrderByOrderNo(orderNo);
            if (order == null) {
                log.warn("订单不存在：{}", orderNo);
                return false;
            }
            
            // 只有退款中状态才能处理
            if (!"REFUNDING".equals(order.getOrderStatus())) {
                log.warn("订单状态不正确，无法处理退款：{}，当前状态：{}", orderNo, order.getOrderStatus());
                return false;
            }
            
            // 验证退款金额
            if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("退款金额无效：{}", refundAmount);
                return false;
            }
            
            if (refundAmount.compareTo(order.getFinalPrice()) > 0) {
                log.warn("退款金额{}超过订单实付金额{}", refundAmount, order.getFinalPrice());
                return false;
            }
            
            // 退款成功，更新订单状态
            order.setOrderStatus("REFUNDED");
            order.setUpdateTime(new Date());
            int rows = orderMapper.updateMembershipOrder(order);
            if (rows > 0) {
                // TODO: 调用微信退款API
                
                // 停用会员（如果会员还在有效期内）
                // membershipService.deactivateMembership(order.getUserId());
                
                log.info("退款处理成功，订单：{}，退款金额：{}", orderNo, refundAmount);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("处理退款失败", e);
            throw new RuntimeException("处理退款失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查超时未支付订单并自动取消
     * 每10分钟执行一次
     */
    @Override
    @Scheduled(cron = "0 */10 * * * ?")
    @Transactional
    public void checkTimeoutOrders() {
        try {
            log.info("开始检查超时订单");
            
            // 查询30分钟前的待支付订单
            List<MembershipOrder> pendingOrders = orderMapper.selectPendingOrdersBeforeTime(30);
            
            int cancelCount = 0;
            
            for (MembershipOrder order : pendingOrders) {
                // 自动取消订单
                String orderNo = order.getOrderNo();
                int rows = orderMapper.cancelOrder(orderNo);
                if (rows > 0) {
                    cancelCount++;
                    log.info("自动取消超时订单：{}，创建时间：{}", order.getOrderNo(), order.getCreateTime());
                }
            }
            
            log.info("超时订单检查完成，共取消{}个订单", cancelCount);
        } catch (Exception e) {
            log.error("检查超时订单失败", e);
        }
    }
    
    /**
     * 查询订单统计信息（全局统计，无需用户ID）
     */
    @Override
    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 查询所有订单
        MembershipOrder query = new MembershipOrder();
        List<MembershipOrder> orders = orderMapper.selectMembershipOrderList(query);
        
        int totalOrders = orders.size();
        int paidOrders = 0;
        int pendingOrders = 0;
        int cancelledOrders = 0;
        int refundedOrders = 0;
        BigDecimal totalSpent = BigDecimal.ZERO;
        
        for (MembershipOrder order : orders) {
            if (order.isPaid()) {
                paidOrders++;
                totalSpent = totalSpent.add(order.getFinalPrice());
            } else if (order.isPending()) {
                pendingOrders++;
            } else if (order.isCancelled()) {
                cancelledOrders++;
            } else if (order.isRefunded()) {
                refundedOrders++;
            }
        }
        
        statistics.put("totalOrders", totalOrders);
        statistics.put("paidOrders", paidOrders);
        statistics.put("pendingOrders", pendingOrders);
        statistics.put("cancelledOrders", cancelledOrders);
        statistics.put("refundedOrders", refundedOrders);
        statistics.put("totalSpent", totalSpent);
        
        return statistics;
    }
    
    /**
     * 查询指定用户在日期范围内的订单总金额
     */
    @Override
    public BigDecimal getTotalAmountByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        // 将LocalDate转换为Date
        Date startDateTime = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDateTime = Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        return orderMapper.selectTotalAmountByUserIdAndDateRange(userId, startDateTime, endDateTime);
    }
    
    /**
     * 查询今日订单数量（返回基本类型int）
     */
    @Override
    public int getTodayOrderCount() {
        MembershipOrder query = new MembershipOrder();
        List<MembershipOrder> orders = orderMapper.selectMembershipOrderList(query);
        
        // 统计今日订单
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayStr = sdf.format(today);
        
        int count = 0;
        for (MembershipOrder order : orders) {
            if (order.getCreateTime() != null) {
                String createDateStr = sdf.format(order.getCreateTime());
                if (todayStr.equals(createDateStr)) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * 查询今日订单总金额
     */
    @Override
    public BigDecimal getTodayOrderAmount() {
        // 获取今日开始和结束时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayStr = sdf.format(new Date());
        try {
            Date startDate = sdf.parse(todayStr);
            Date endDate = new Date(startDate.getTime() + 24 * 60 * 60 * 1000 - 1);
            BigDecimal amount = orderMapper.selectTotalAmountByUserIdAndDateRange(null, startDate, endDate);
            return amount != null ? amount : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("查询今日订单金额失败", e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 检查用户是否有待支付订单
     */
    @Override
    public boolean hasPendingOrder(Long userId) {
        int count = orderMapper.countOrdersByUserIdAndStatus(userId, "PENDING");
        return count > 0;
    }
    
    /**
     * 生成订单号
     * 格式：ORD + 年月日时分秒 + 6位随机数
     * 例如：ORD20260117150030123456
     */
    @Override
    public String generateOrderNo() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStr = formatter.format(new Date());
        
        // 生成6位随机数
        Random random = new Random();
        int randomNum = random.nextInt(900000) + 100000;
        
        return "ORD" + timeStr + randomNum;
    }
    
    /**
     * 获取套餐原价
     */
    @Override
    public BigDecimal getPackageOriginalPrice(String packageType) {
        if ("WEEK".equals(packageType)) {
            return new BigDecimal("5.90");
        } else if ("MONTH".equals(packageType)) {
            return new BigDecimal("19.90");
        }
        throw new RuntimeException("无效的套餐类型：" + packageType);
    }
    
    /**
     * 获取套餐现价（固定优惠价格）
     */
    @Override
    public BigDecimal getPackagePrice(String packageType) {
        if ("WEEK".equals(packageType)) {
            return new BigDecimal("2.90");
        } else if ("MONTH".equals(packageType)) {
            return new BigDecimal("9.90");
        }
        throw new RuntimeException("无效的套餐类型：" + packageType);
    }
}
