package com.ruoyi.web.service;

import com.ruoyi.web.domain.WechatUser;

/**
 * 微信登录Service接口
 * 
 * @author ruoyi
 * @date 2026-01-11
 */
public interface IWechatLoginService {
    
    /**
     * 微信小程序登录
     * 
     * @param code 微信登录凭证code
     * @param nickName 用户昵称
     * @param avatarUrl 用户头像URL
     * @param gender 性别(0-未知, 1-男, 2-女)
     * @param country 国家
     * @param province 省份
     * @param city 城市
     * @return 登录结果，包含token和用户信息
     */
    public WechatLoginResult login(String code, String nickName, String avatarUrl, 
                                    Integer gender, String country, String province, String city);
    
    /**
     * 微信小程序登录（带设备信息）
     * 
     * @param code 微信登录凭证code
     * @param nickName 用户昵称
     * @param avatarUrl 用户头像URL
     * @param gender 性别(0-未知, 1-男, 2-女)
     * @param country 国家
     * @param province 省份
     * @param city 城市
     * @param deviceModel 设备型号
     * @param deviceBrand 设备品牌
     * @param systemVersion 系统版本
     * @param platform 平台
     * @param screenWidth 屏幕宽度
     * @param screenHeight 屏幕高度
     * @param pixelRatio 像素比
     * @param language 语言
     * @param wechatVersion 微信版本
     * @param sdkVersion SDK版本
     * @param fontSizeSetting 字体大小设置
     * @param benchmarkLevel 性能等级
     * @param batteryLevel 电池电量
     * @param networkType 网络类型
     * @return 登录结果，包含token和用户信息
     */
    public WechatLoginResult loginWithDeviceInfo(String code, String nickName, String avatarUrl, 
                                                  Integer gender, String country, String province, String city,
                                                  String deviceModel, String deviceBrand, String systemVersion, 
                                                  String platform, String screenWidth, String screenHeight,
                                                  String pixelRatio, String language, String wechatVersion,
                                                  String sdkVersion, String fontSizeSetting, String benchmarkLevel,
                                                  String batteryLevel, String networkType);
    
    /**
     * 根据token获取用户信息
     * 
     * @param token JWT token
     * @return 用户信息
     */
    public WechatUser getUserByToken(String token);
    
    /**
     * 刷新token
     * 
     * @param token 旧的JWT token
     * @return 新的token
     */
    public String refreshToken(String token);
    
    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param nickName 用户昵称
     * @param avatarUrl 用户头像URL
     * @param gender 性别
     * @param phone 手机号
     * @return 更新结果
     */
    public int updateUserInfo(Long userId, String nickName, String avatarUrl, 
                              Integer gender, String phone);
    
    /**
     * 验证token是否有效
     * 
     * @param token JWT token
     * @return true-有效, false-无效
     */
    public boolean validateToken(String token);
    
    /**
     * 登出
     * 
     * @param userId 用户ID
     * @return 登出结果
     */
    public void logout(Long userId);
    
    /**
     * 登录结果内部类
     */
    public static class WechatLoginResult {
        /** JWT token */
        private String token;
        
        /** 用户信息 */
        private WechatUser user;
        
        /** 是否为新用户 */
        private Boolean isNewUser;
        
        public WechatLoginResult() {}
        
        public WechatLoginResult(String token, WechatUser user, Boolean isNewUser) {
            this.token = token;
            this.user = user;
            this.isNewUser = isNewUser;
        }
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
        
        public WechatUser getUser() {
            return user;
        }
        
        public void setUser(WechatUser user) {
            this.user = user;
        }
        
        public Boolean getIsNewUser() {
            return isNewUser;
        }
        
        public void setIsNewUser(Boolean isNewUser) {
            this.isNewUser = isNewUser;
        }
    }
}
