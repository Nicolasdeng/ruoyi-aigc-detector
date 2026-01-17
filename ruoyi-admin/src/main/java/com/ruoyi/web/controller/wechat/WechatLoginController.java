package com.ruoyi.web.controller.wechat;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.web.annotation.RateLimit;
import com.ruoyi.web.annotation.RateLimit.LimitType;
import com.ruoyi.web.domain.WechatUser;
import com.ruoyi.web.service.ITokenBlacklistService;
import com.ruoyi.web.service.IWechatLoginService;
import com.ruoyi.web.service.IWechatLoginService.WechatLoginResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 微信登录控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/wechat")
public class WechatLoginController extends BaseController {
    
    private static final Logger log = LoggerFactory.getLogger(WechatLoginController.class);
    
    @Autowired
    private IWechatLoginService wechatLoginService;
    
    @Autowired
    private ITokenBlacklistService tokenBlacklistService;
    
    @Autowired
    private com.ruoyi.web.utils.JwtTokenUtil jwtTokenUtil;
    
    /**
     * 微信小程序登录（支持设备信息）
     * 
     * @param code 微信登录凭证
     * @param nickName 用户昵称
     * @param avatarUrl 用户头像
     * @param gender 性别（0-未知，1-男，2-女）
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
     * @param batteryLevel 电量
     * @param networkType 网络类型
     * @return 登录结果（包含token和用户信息）
     */
    @PostMapping("/login")
    @RateLimit(key = "wechat:login", time = 60, count = 10, limitType = LimitType.IP)
    public AjaxResult login(
            @RequestParam String code,
            @RequestParam(required = false) String nickName,
            @RequestParam(required = false) String avatarUrl,
            @RequestParam(required = false, defaultValue = "0") Integer gender,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String deviceModel,
            @RequestParam(required = false) String deviceBrand,
            @RequestParam(required = false) String systemVersion,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String screenWidth,
            @RequestParam(required = false) String screenHeight,
            @RequestParam(required = false) String pixelRatio,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String wechatVersion,
            @RequestParam(required = false) String sdkVersion,
            @RequestParam(required = false) String fontSizeSetting,
            @RequestParam(required = false) String benchmarkLevel,
            @RequestParam(required = false) String batteryLevel,
            @RequestParam(required = false) String networkType) {
        try {
            log.info("微信登录请求: code={}, nickName={}", code, nickName);
            
            // 使用基础login方法
            WechatLoginResult result = wechatLoginService.login(
                code, nickName, avatarUrl, gender, country, province, city);
            
            log.info("微信登录成功: userId={}, isNewUser={}, deviceModel={}", 
                result.getUser().getUserId(), result.getIsNewUser(), deviceModel);
            
            return AjaxResult.success()
                    .put("token", result.getToken())
                    .put("user", result.getUser())
                    .put("isNewUser", result.getIsNewUser());
        } catch (Exception e) {
            log.error("微信登录失败: {}", e.getMessage(), e);
            return AjaxResult.error("登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 微信小程序登录（带设备信息，推荐使用）
     * 
     * @param code 微信登录凭证
     * @param nickName 用户昵称
     * @param avatarUrl 用户头像
     * @param gender 性别（0-未知，1-男，2-女）
     * @param country 国家
     * @param province 省份
     * @param city 城市
     * @param deviceType 设备类型（如：miniapp、ios、android）
     * @param deviceModel 设备型号
     * @param systemVersion 系统版本
     * @param appVersion 应用版本
     * @return 登录结果（包含token和用户信息）
     */
    @PostMapping("/loginWithDevice")
    @RateLimit(key = "wechat:login:device", time = 60, count = 10, limitType = LimitType.IP)
    public AjaxResult loginWithDevice(
            @RequestParam String code,
            @RequestParam(required = false) String nickName,
            @RequestParam(required = false) String avatarUrl,
            @RequestParam(required = false, defaultValue = "0") Integer gender,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String deviceModel,
            @RequestParam(required = false) String systemVersion,
            @RequestParam(required = false) String appVersion) {
        try {
            log.info("微信登录请求(带设备信息): code={}, nickName={}, deviceType={}, deviceModel={}", 
                code, nickName, deviceType, deviceModel);
            
            // 使用基础login方法
            WechatLoginResult result = wechatLoginService.login(
                code, nickName, avatarUrl, gender, country, province, city);
            
            log.info("微信登录成功: userId={}, isNewUser={}, deviceType={}", 
                result.getUser().getUserId(), result.getIsNewUser(), deviceType);
            
            return AjaxResult.success()
                    .put("token", result.getToken())
                    .put("user", result.getUser())
                    .put("isNewUser", result.getIsNewUser());
        } catch (Exception e) {
            log.error("微信登录失败: {}", e.getMessage(), e);
            return AjaxResult.error("登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户信息
     * 
     * @param token JWT Token
     * @return 用户信息
     */
    @GetMapping("/userInfo")
    public AjaxResult getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            // 移除 "Bearer " 前缀（如果有）
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            WechatUser user = wechatLoginService.getUserByToken(token);
            if (user == null) {
                return AjaxResult.error("Token无效或已过期");
            }
            
            return AjaxResult.success(user);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage(), e);
            return AjaxResult.error("获取用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 刷新Token
     * 
     * @param token 当前Token
     * @return 新Token
     */
    @PostMapping("/refreshToken")
    @RateLimit(key = "wechat:refresh", time = 60, count = 20, limitType = LimitType.USER)
    public AjaxResult refreshToken(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 检查Token是否为空
            if (token == null || token.trim().isEmpty()) {
                return AjaxResult.error("Token不能为空");
            }
            
            // 移除 "Bearer " 前缀（如果有）
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            String newToken = wechatLoginService.refreshToken(token);
            if (newToken == null) {
                return AjaxResult.error("Token刷新失败");
            }
            
            return AjaxResult.success().put("token", newToken);
        } catch (Exception e) {
            log.error("刷新Token失败: {}", e.getMessage(), e);
            return AjaxResult.error("刷新Token失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户信息
     * 
     * @param token JWT Token
     * @param nickName 用户昵称
     * @param avatarUrl 用户头像
     * @param gender 性别
     * @param phone 手机号
     * @return 更新结果
     */
    @PutMapping("/updateUserInfo")
    public AjaxResult updateUserInfo(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String nickName,
            @RequestParam(required = false) String avatarUrl,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) String phone) {
        try {
            // 移除 "Bearer " 前缀（如果有）
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // 从token中获取userId
            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                return AjaxResult.error("Token无效");
            }
            
            int rows = wechatLoginService.updateUserInfo(userId, nickName, avatarUrl, gender, phone);
            if (rows > 0) {
                return AjaxResult.success("用户信息更新成功");
            } else {
                return AjaxResult.error("用户信息更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户信息失败: {}", e.getMessage(), e);
            return AjaxResult.error("更新用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证Token是否有效
     * 
     * @param token JWT Token
     * @return 验证结果
     */
    @PostMapping("/validateToken")
    public AjaxResult validateToken(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 检查Token是否为空
            if (token == null || token.trim().isEmpty()) {
                return AjaxResult.success().put("isValid", false);
            }
            
            // 移除 "Bearer " 前缀（如果有）
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            boolean isValid = wechatLoginService.validateToken(token);
            return AjaxResult.success().put("isValid", isValid);
        } catch (Exception e) {
            log.error("验证Token失败: {}", e.getMessage(), e);
            return AjaxResult.error("验证Token失败: " + e.getMessage());
        }
    }
    
    /**
     * 登出
     * 
     * @param token JWT Token
     * @return 登出结果
     */
    @PostMapping("/logout")
    @RateLimit(key = "wechat:logout", time = 60, count = 10, limitType = LimitType.USER)
    public AjaxResult logout(@RequestHeader("Authorization") String token) {
        try {
            // 移除 "Bearer " 前缀（如果有）
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // 从token获取userId
            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId != null) {
                wechatLoginService.logout(userId);
            }
            
            // 将Token加入黑名单，计算过期时间（秒）
            long expireSeconds = 0;
            try {
                java.util.Date expireDate = jwtTokenUtil.getExpirationDateFromToken(token);
                if (expireDate != null) {
                    expireSeconds = (expireDate.getTime() - System.currentTimeMillis()) / 1000;
                    if (expireSeconds < 0) {
                        expireSeconds = 0;
                    }
                }
            } catch (Exception e) {
                log.warn("计算Token过期时间失败，使用默认值: {}", e.getMessage());
                expireSeconds = 7 * 24 * 60 * 60; // 默认7天
            }
            tokenBlacklistService.addToBlacklist(token, expireSeconds);
            
            log.info("用户登出成功，Token已加入黑名单");
            return AjaxResult.success("登出成功");
        } catch (Exception e) {
            log.error("登出失败: {}", e.getMessage(), e);
            return AjaxResult.error("登出失败: " + e.getMessage());
        }
    }
    
    /**
     * 强制下线（管理员功能）
     * 将指定用户的所有Token加入黑名单
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/forceLogout/{userId}")
    @RateLimit(key = "wechat:force:logout", time = 60, count = 5, limitType = LimitType.IP)
    public AjaxResult forceLogout(@PathVariable Long userId) {
        try {
            // 强制下线时，设置较长的黑名单有效期（7天）
            long expireSeconds = 7 * 24 * 60 * 60;
            tokenBlacklistService.addUserTokensToBlacklist(userId, expireSeconds);
            log.info("强制下线用户: userId={}", userId);
            return AjaxResult.success("用户已强制下线");
        } catch (Exception e) {
            log.error("强制下线失败: {}", e.getMessage(), e);
            return AjaxResult.error("强制下线失败: " + e.getMessage());
        }
    }
}
