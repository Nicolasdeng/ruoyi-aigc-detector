package com.ruoyi.web.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 登录日志实体类
 * 
 * @author ruoyi
 */
@Data
public class SysLoginLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 用户ID */
    private Long userId;

    /** 微信openid */
    private String openid;

    /** 登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date loginTime;

    /** 登录IP */
    private String loginIp;

    /** 登录地点 */
    private String loginLocation;

    /** 设备类型 */
    private String deviceType;

    /** 设备型号 */
    private String deviceModel;

    /** 系统版本 */
    private String systemVersion;

    /** 小程序版本 */
    private String appVersion;

    /** 登录结果(0成功 1失败) */
    private String loginResult;

    /** 失败原因 */
    private String failReason;

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public String getLoginLocation() {
        return loginLocation;
    }

    public void setLoginLocation(String loginLocation) {
        this.loginLocation = loginLocation;
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

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getLoginResult() {
        return loginResult;
    }

    public void setLoginResult(String loginResult) {
        this.loginResult = loginResult;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    @Override
    public String toString() {
        return "SysLoginLog{" +
                "logId=" + logId +
                ", userId=" + userId +
                ", openid='" + openid + '\'' +
                ", loginTime=" + loginTime +
                ", loginIp='" + loginIp + '\'' +
                ", loginLocation='" + loginLocation + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", systemVersion='" + systemVersion + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", loginResult='" + loginResult + '\'' +
                ", failReason='" + failReason + '\'' +
                '}';
    }
}
