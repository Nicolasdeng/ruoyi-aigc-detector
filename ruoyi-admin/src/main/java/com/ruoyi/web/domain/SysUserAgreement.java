package com.ruoyi.web.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户协议实体类
 * 
 * @author ruoyi
 */
public class SysUserAgreement implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /** 协议类型常量: 用户协议 */
    public static final Integer USER_AGREEMENT = 1;
    
    /** 协议类型常量: 隐私政策 */
    public static final Integer PRIVACY_POLICY = 2;

    /** 协议ID */
    private Long agreementId;

    /** 协议类型(1=用户协议 2=隐私政策) */
    private Integer agreementType;

    /** 协议标题 */
    private String title;

    /** 协议内容 */
    private String content;

    /** 版本号 */
    private String version;

    /** 是否生效(0=否 1=是) */
    private Integer isActive;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 创建人 */
    private String createBy;

    /** 更新人 */
    private String updateBy;

    public Long getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(Long agreementId) {
        this.agreementId = agreementId;
    }

    public Integer getAgreementType() {
        return agreementType;
    }

    public void setAgreementType(Integer agreementType) {
        this.agreementType = agreementType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    @Override
    public String toString() {
        return "SysUserAgreement{" +
                "agreementId=" + agreementId +
                ", agreementType=" + agreementType +
                ", title='" + title + '\'' +
                ", version='" + version + '\'' +
                ", isActive=" + isActive +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
