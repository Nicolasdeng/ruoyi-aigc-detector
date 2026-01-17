package com.ruoyi.web.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 微信小程序用户对象 wechat_user
 * 
 * @author ruoyi
 * @date 2026-01-11
 */
public class WechatUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 微信openid */
    @Excel(name = "微信openid")
    private String openid;

    /** 微信unionid */
    @Excel(name = "微信unionid")
    private String unionid;

    /** 微信会话密钥 */
    private String sessionKey;

    /** 用户昵称 */
    @Excel(name = "用户昵称")
    private String nickName;

    /** 用户头像URL */
    @Excel(name = "用户头像URL")
    private String avatarUrl;

    /** 性别 0-未知 1-男 2-女 */
    @Excel(name = "性别", readConverterExp = "0=未知,1=男,2=女")
    private Integer gender;

    /** 国家 */
    @Excel(name = "国家")
    private String country;

    /** 省份 */
    @Excel(name = "省份")
    private String province;

    /** 城市 */
    @Excel(name = "城市")
    private String city;

    /** 手机号 */
    @Excel(name = "手机号")
    private String phone;

    /** 账号状态 0-正常 1-停用 */
    @Excel(name = "账号状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志 0-存在 2-删除 */
    private String delFlag;

    /** 最后登录IP */
    @Excel(name = "最后登录IP")
    private String loginIp;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date loginDate;

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setOpenid(String openid) 
    {
        this.openid = openid;
    }

    public String getOpenid() 
    {
        return openid;
    }

    public void setUnionid(String unionid) 
    {
        this.unionid = unionid;
    }

    public String getUnionid() 
    {
        return unionid;
    }

    public void setSessionKey(String sessionKey) 
    {
        this.sessionKey = sessionKey;
    }

    public String getSessionKey() 
    {
        return sessionKey;
    }

    public void setNickName(String nickName) 
    {
        this.nickName = nickName;
    }

    public String getNickName() 
    {
        return nickName;
    }

    public void setAvatarUrl(String avatarUrl) 
    {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() 
    {
        return avatarUrl;
    }

    public void setGender(Integer gender) 
    {
        this.gender = gender;
    }

    public Integer getGender() 
    {
        return gender;
    }

    public void setCountry(String country) 
    {
        this.country = country;
    }

    public String getCountry() 
    {
        return country;
    }

    public void setProvince(String province) 
    {
        this.province = province;
    }

    public String getProvince() 
    {
        return province;
    }

    public void setCity(String city) 
    {
        this.city = city;
    }

    public String getCity() 
    {
        return city;
    }

    public void setPhone(String phone) 
    {
        this.phone = phone;
    }

    public String getPhone() 
    {
        return phone;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    public void setLoginIp(String loginIp) 
    {
        this.loginIp = loginIp;
    }

    public String getLoginIp() 
    {
        return loginIp;
    }

    public void setLoginDate(Date loginDate) 
    {
        this.loginDate = loginDate;
    }

    public Date getLoginDate() 
    {
        return loginDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("userId", getUserId())
            .append("openid", getOpenid())
            .append("unionid", getUnionid())
            .append("sessionKey", getSessionKey())
            .append("nickName", getNickName())
            .append("avatarUrl", getAvatarUrl())
            .append("gender", getGender())
            .append("country", getCountry())
            .append("province", getProvince())
            .append("city", getCity())
            .append("phone", getPhone())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("loginIp", getLoginIp())
            .append("loginDate", getLoginDate())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
