package com.ruoyi.web.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 用户每日配额对象 user_daily_quota
 * 
 * @author ruoyi
 * @date 2026-01-17
 */
public class UserDailyQuota {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 配额日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date quotaDate;

    /** 文本检测配额 */
    private Integer textQuota;

    /** 文本检测已使用 */
    private Integer textUsed;

    /** 图片检测配额 */
    private Integer imageQuota;

    /** 图片检测已使用 */
    private Integer imageUsed;

    /** 视频检测配额 */
    private Integer videoQuota;

    /** 视频检测已使用 */
    private Integer videoUsed;

    /** 音频检测配额 */
    private Integer audioQuota;

    /** 音频检测已使用 */
    private Integer audioUsed;

    /** 论文检测配额 */
    private Integer paperQuota;

    /** 论文检测已使用 */
    private Integer paperUsed;

    /** 看广告获得的额外配额（未来功能预留） */
    private Integer adBonusQuota;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setQuotaDate(Date quotaDate) {
        this.quotaDate = quotaDate;
    }

    public Date getQuotaDate() {
        return quotaDate;
    }

    public void setTextQuota(Integer textQuota) {
        this.textQuota = textQuota;
    }

    public Integer getTextQuota() {
        return textQuota;
    }

    public void setTextUsed(Integer textUsed) {
        this.textUsed = textUsed;
    }

    public Integer getTextUsed() {
        return textUsed;
    }

    public void setImageQuota(Integer imageQuota) {
        this.imageQuota = imageQuota;
    }

    public Integer getImageQuota() {
        return imageQuota;
    }

    public void setImageUsed(Integer imageUsed) {
        this.imageUsed = imageUsed;
    }

    public Integer getImageUsed() {
        return imageUsed;
    }

    public void setVideoQuota(Integer videoQuota) {
        this.videoQuota = videoQuota;
    }

    public Integer getVideoQuota() {
        return videoQuota;
    }

    public void setVideoUsed(Integer videoUsed) {
        this.videoUsed = videoUsed;
    }

    public Integer getVideoUsed() {
        return videoUsed;
    }

    public void setAudioQuota(Integer audioQuota) {
        this.audioQuota = audioQuota;
    }

    public Integer getAudioQuota() {
        return audioQuota;
    }

    public void setAudioUsed(Integer audioUsed) {
        this.audioUsed = audioUsed;
    }

    public Integer getAudioUsed() {
        return audioUsed;
    }

    public void setPaperQuota(Integer paperQuota) {
        this.paperQuota = paperQuota;
    }

    public Integer getPaperQuota() {
        return paperQuota;
    }

    public void setPaperUsed(Integer paperUsed) {
        this.paperUsed = paperUsed;
    }

    public Integer getPaperUsed() {
        return paperUsed;
    }

    public void setAdBonusQuota(Integer adBonusQuota) {
        this.adBonusQuota = adBonusQuota;
    }

    public Integer getAdBonusQuota() {
        return adBonusQuota;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 获取文本检测剩余配额
     * 注意：广告激励功能预留，当前未启用
     */
    public Integer getRemainingTextQuota() {
        return (textQuota != null ? textQuota : 0) - (textUsed != null ? textUsed : 0);
    }

    /**
     * 获取图片检测剩余配额
     */
    public Integer getRemainingImageQuota() {
        return (imageQuota != null ? imageQuota : 0) - (imageUsed != null ? imageUsed : 0);
    }

    /**
     * 获取视频检测剩余配额
     */
    public Integer getRemainingVideoQuota() {
        return (videoQuota != null ? videoQuota : 0) - (videoUsed != null ? videoUsed : 0);
    }

    /**
     * 获取音频检测剩余配额
     */
    public Integer getRemainingAudioQuota() {
        return (audioQuota != null ? audioQuota : 0) - (audioUsed != null ? audioUsed : 0);
    }

    /**
     * 获取论文检测剩余配额
     */
    public Integer getRemainingPaperQuota() {
        return (paperQuota != null ? paperQuota : 0) - (paperUsed != null ? paperUsed : 0);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("quotaDate", getQuotaDate())
            .append("textQuota", getTextQuota())
            .append("textUsed", getTextUsed())
            .append("imageQuota", getImageQuota())
            .append("imageUsed", getImageUsed())
            .append("videoQuota", getVideoQuota())
            .append("videoUsed", getVideoUsed())
            .append("audioQuota", getAudioQuota())
            .append("audioUsed", getAudioUsed())
            .append("paperQuota", getPaperQuota())
            .append("paperUsed", getPaperUsed())
            .append("adBonusQuota", getAdBonusQuota())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
