package com.ruoyi.web.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 用户会员信息实体类
 * 
 * @author ruoyi
 */
public class UserMembership {
    
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 会员类型：FREE-免费会员, GOLD-黄金会员 */
    private String membershipType;

    /** 会员过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    /** 是否激活：0-未激活, 1-已激活 */
    private Integer isActive;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
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
     * 判断会员是否已过期
     */
    public boolean isExpired() {
        if (expireTime == null) {
            return true;
        }
        return new Date().after(expireTime);
    }

    /**
     * 判断是否为免费会员
     */
    public boolean isFree() {
        return "FREE".equals(membershipType);
    }

    /**
     * 判断是否为黄金会员
     */
    public boolean isGold() {
        return "GOLD".equals(membershipType);
    }

    /**
     * 判断是否为付费会员（黄金会员）
     */
    public boolean isPaidMember() {
        return isGold();
    }

    /**
     * 判断会员是否有效（已激活且未过期）
     */
    public boolean isValid() {
        return isActive != null && isActive == 1 && !isExpired();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("userId", getUserId())
                .append("membershipType", getMembershipType())
                .append("expireTime", getExpireTime())
                .append("isActive", getIsActive())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
