package com.ruoyi.web.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户协议确认记录实体类
 * 
 * @author ruoyi
 */
public class SysUserAgreementRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 用户ID */
    private Long userId;

    /** 协议ID */
    private Long agreementId;

    /** 协议版本号 */
    private String agreementVersion;

    /** 确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date confirmTime;

    /** 确认IP */
    private String confirmIp;

    /** 设备类型 */
    private String deviceType;

    /** 设备型号 */
    private String deviceModel;

    /** 是否同意(0=否 1=是) */
    private Integer isAgreed;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(Long agreementId) {
        this.agreementId = agreementId;
    }

    public String getAgreementVersion() {
        return agreementVersion;
    }

    public void setAgreementVersion(String agreementVersion) {
        this.agreementVersion = agreementVersion;
    }

    public Date getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(Date confirmTime) {
        this.confirmTime = confirmTime;
    }

    public String getConfirmIp() {
        return confirmIp;
    }

    public void setConfirmIp(String confirmIp) {
        this.confirmIp = confirmIp;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public Integer getIsAgreed() {
        return isAgreed;
    }

    public void setIsAgreed(Integer isAgreed) {
        this.isAgreed = isAgreed;
    }

    @Override
    public String toString() {
        return "SysUserAgreementRecord{" +
                "recordId=" + recordId +
                ", userId=" + userId +
                ", agreementId=" + agreementId +
                ", agreementVersion='" + agreementVersion + '\'' +
                ", confirmTime=" + confirmTime +
                ", confirmIp='" + confirmIp + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", isAgreed=" + isAgreed +
                '}';
    }
}
