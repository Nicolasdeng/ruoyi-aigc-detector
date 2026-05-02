package com.ruoyi.web.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员订单实体类
 * 
 * @author ruoyi
 */
public class MembershipOrder {
    
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 订单号（唯一） */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 套餐类型：WEEK-周卡, MONTH-月卡 */
    private String packageType;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 实付金额 */
    private BigDecimal finalPrice;

    /** 支付方式：WECHAT_PAY-微信支付 */
    private String paymentMethod;

    /** 微信支付交易单号 */
    private String transactionId;

    /** 订单状态：PENDING-待支付, PAID-已支付, CANCELLED-已取消, REFUNDED-已退款 */
    private String orderStatus;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date paidTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    // Getter and Setter methods

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Date getPaidTime() {
        return paidTime;
    }

    public void setPaidTime(Date paidTime) {
        this.paidTime = paidTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 判断订单是否待支付
     */
    public boolean isPending() {
        return "PENDING".equals(orderStatus);
    }

    /**
     * 判断订单是否已支付
     */
    public boolean isPaid() {
        return "PAID".equals(orderStatus);
    }

    /**
     * 判断订单是否已取消
     */
    public boolean isCancelled() {
        return "CANCELLED".equals(orderStatus);
    }

    /**
     * 判断订单是否已退款
     */
    public boolean isRefunded() {
        return "REFUNDED".equals(orderStatus);
    }

    /**
     * 判断是否为周卡套餐
     */
    public boolean isWeekPackage() {
        return "WEEK".equals(packageType);
    }

    /**
     * 判断是否为月卡套餐
     */
    public boolean isMonthPackage() {
        return "MONTH".equals(packageType);
    }

    /**
     * 获取订单金额（单位：分）
     * 用于微信支付，将BigDecimal金额转换为以分为单位的Long类型
     * 
     * @return 订单金额（分）
     */
    public Long getAmount() {
        if (finalPrice == null) {
            return 0L;
        }
        // 将元转换为分（乘以100）
        return finalPrice.multiply(new BigDecimal("100")).longValue();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("orderNo", getOrderNo())
                .append("userId", getUserId())
                .append("packageType", getPackageType())
                .append("originalPrice", getOriginalPrice())
                .append("finalPrice", getFinalPrice())
                .append("paymentMethod", getPaymentMethod())
                .append("transactionId", getTransactionId())
                .append("orderStatus", getOrderStatus())
                .append("paidTime", getPaidTime())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
