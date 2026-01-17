package com.ruoyi.web.service.impl;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.web.domain.SysLoginLog;
import com.ruoyi.web.domain.WechatUser;
import com.ruoyi.web.mapper.WechatUserMapper;
import com.ruoyi.web.service.ILoginLogService;
import com.ruoyi.web.service.IWechatLoginService;
import com.ruoyi.web.utils.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 微信登录Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-01-11
 */
@Service
public class WechatLoginServiceImpl implements IWechatLoginService {
    
    private static final Logger log = LoggerFactory.getLogger(WechatLoginServiceImpl.class);
    
    @Autowired
    private WechatUserMapper wechatUserMapper;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ILoginLogService loginLogService;
    
    /** 微信小程序AppID */
    @Value("${wechat.miniapp.appid:}")
    private String appId;
    
    /** 微信小程序AppSecret */
    @Value("${wechat.miniapp.secret:}")
    private String appSecret;
    
    /** 微信登录API地址 */
    private static final String WECHAT_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    
    /**
     * 微信小程序登录
     */
    @Override
    @Transactional
    public WechatLoginResult login(String code, String nickName, String avatarUrl, 
                                    Integer gender, String country, String province, String city) {
        return loginWithDeviceInfo(code, nickName, avatarUrl, gender, country, province, city, 
                                   null, null, null, null, null, null, null, null, null, 
                                   null, null, null, null, null);
    }
    
    /**
     * 微信小程序登录（带设备信息）
     */
    @Override
    @Transactional
    public WechatLoginResult loginWithDeviceInfo(String code, String nickName, String avatarUrl, 
                                                  Integer gender, String country, String province, String city,
                                                  String deviceModel, String deviceBrand, String systemVersion, 
                                                  String platform, String screenWidth, String screenHeight,
                                                  String pixelRatio, String language, String wechatVersion,
                                                  String sdkVersion, String fontSizeSetting, String benchmarkLevel,
                                                  String batteryLevel, String networkType) {
        String loginIp = getClientIp();
        String openid = null;
        
        // 1. 参数校验
        if (StringUtils.isEmpty(code)) {
            recordLoginLog(null, null, loginIp, deviceModel, deviceBrand, systemVersion, platform,
                          screenWidth, screenHeight, pixelRatio, language, wechatVersion, sdkVersion,
                          fontSizeSetting, benchmarkLevel, batteryLevel, networkType,
                          "fail", "登录code不能为空");
            throw new RuntimeException("登录code不能为空");
        }
        
        if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(appSecret)) {
            recordLoginLog(null, null, loginIp, deviceModel, deviceBrand, systemVersion, platform,
                          screenWidth, screenHeight, pixelRatio, language, wechatVersion, sdkVersion,
                          fontSizeSetting, benchmarkLevel, batteryLevel, networkType,
                          "fail", "微信小程序配置未完成");
            throw new RuntimeException("微信小程序配置未完成，请联系管理员");
        }
        
        // 2. 调用微信API获取openid和session_key
        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                WECHAT_LOGIN_URL, appId, appSecret, code);
        
        String response = null;
        try {
            response = restTemplate.getForObject(url, String.class);
            log.info("微信登录API响应: {}", response);
        } catch (Exception e) {
            log.error("调用微信登录API失败", e);
            recordLoginLog(null, null, loginIp, deviceModel, deviceBrand, systemVersion, platform,
                          screenWidth, screenHeight, pixelRatio, language, wechatVersion, sdkVersion,
                          fontSizeSetting, benchmarkLevel, batteryLevel, networkType,
                          "fail", "调用微信API失败: " + e.getMessage());
            throw new RuntimeException("微信登录失败，请稍后重试");
        }
        
        if (StringUtils.isEmpty(response)) {
            recordLoginLog(null, null, loginIp, deviceModel, deviceBrand, systemVersion, platform,
                          screenWidth, screenHeight, pixelRatio, language, wechatVersion, sdkVersion,
                          fontSizeSetting, benchmarkLevel, batteryLevel, networkType,
                          "fail", "微信API返回为空");
            throw new RuntimeException("微信登录失败，请稍后重试");
        }
        
        // 3. 解析响应
        JSONObject jsonObject = JSON.parseObject(response);
        Integer errcode = jsonObject.getInteger("errcode");
        
        if (errcode != null && errcode != 0) {
            String errmsg = jsonObject.getString("errmsg");
            log.error("微信登录失败: errcode={}, errmsg={}", errcode, errmsg);
            recordLoginLog(null, null, loginIp, deviceModel, deviceBrand, systemVersion, platform,
                          screenWidth, screenHeight, pixelRatio, language, wechatVersion, sdkVersion,
                          fontSizeSetting, benchmarkLevel, batteryLevel, networkType,
                          "fail", "微信API错误: " + errmsg);
            throw new RuntimeException("微信登录失败: " + errmsg);
        }
        
        openid = jsonObject.getString("openid");
        String sessionKey = jsonObject.getString("session_key");
        String unionid = jsonObject.getString("unionid");
        
        if (StringUtils.isEmpty(openid)) {
            recordLoginLog(null, null, loginIp, deviceModel, deviceBrand, systemVersion, platform,
                          screenWidth, screenHeight, pixelRatio, language, wechatVersion, sdkVersion,
                          fontSizeSetting, benchmarkLevel, batteryLevel, networkType,
                          "fail", "获取openid失败");
            throw new RuntimeException("获取微信openid失败");
        }
        
        // 4. 查询用户是否已存在
        WechatUser existUser = wechatUserMapper.selectWechatUserByOpenid(openid);
        boolean isNewUser = (existUser == null);
        
        WechatUser wechatUser;
        if (isNewUser) {
            // 5. 新用户注册
            wechatUser = new WechatUser();
            wechatUser.setOpenid(openid);
            wechatUser.setUnionid(unionid);
            wechatUser.setSessionKey(sessionKey);
            wechatUser.setNickName(nickName);
            wechatUser.setAvatarUrl(avatarUrl);
            wechatUser.setGender(gender);
            wechatUser.setCountry(country);
            wechatUser.setProvince(province);
            wechatUser.setCity(city);
            wechatUser.setStatus("0"); // 正常状态
            wechatUser.setDelFlag("0"); // 未删除
            wechatUser.setLoginIp(loginIp);
            wechatUser.setLoginDate(new Date());
            wechatUser.setCreateTime(new Date());
            
            wechatUserMapper.insertWechatUser(wechatUser);
            log.info("新用户注册成功: userId={}, openid={}", wechatUser.getUserId(), openid);
        } else {
            // 6. 老用户更新信息
            wechatUser = existUser;
            wechatUser.setSessionKey(sessionKey);
            
            // 更新用户信息（如果提供了新的信息）
            if (StringUtils.isNotEmpty(nickName)) {
                wechatUser.setNickName(nickName);
            }
            if (StringUtils.isNotEmpty(avatarUrl)) {
                wechatUser.setAvatarUrl(avatarUrl);
            }
            if (gender != null) {
                wechatUser.setGender(gender);
            }
            
            wechatUser.setLoginIp(loginIp);
            wechatUser.setLoginDate(new Date());
            wechatUser.setUpdateTime(new Date());
            
            wechatUserMapper.updateLoginInfo(wechatUser);
            log.info("用户登录成功: userId={}, openid={}", wechatUser.getUserId(), openid);
        }
        
        // 7. 生成JWT Token
        String token = jwtTokenUtil.generateToken(wechatUser.getUserId(), openid);
        
        // 8. 记录登录成功日志
        recordLoginLog(wechatUser.getUserId(), openid, loginIp, deviceModel, deviceBrand, systemVersion, 
                      platform, screenWidth, screenHeight, pixelRatio, language, wechatVersion, 
                      sdkVersion, fontSizeSetting, benchmarkLevel, batteryLevel, networkType,
                      "success", null);
        
        // 9. 返回登录结果
        WechatLoginResult result = new WechatLoginResult(token, wechatUser, isNewUser);
        return result;
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return IpUtils.getIpAddr(request);
            }
        } catch (Exception e) {
            log.warn("获取客户端IP失败", e);
        }
        return "unknown";
    }
    
    /**
     * 记录登录日志
     */
    private void recordLoginLog(Long userId, String openid, String loginIp, 
                                String deviceModel, String deviceBrand, String systemVersion, 
                                String platform, String screenWidth, String screenHeight,
                                String pixelRatio, String language, String wechatVersion,
                                String sdkVersion, String fontSizeSetting, String benchmarkLevel,
                                String batteryLevel, String networkType,
                                String loginResult, String failReason) {
        try {
            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setUserId(userId);
            loginLog.setOpenid(openid);
            loginLog.setLoginTime(new Date());
            loginLog.setLoginIp(loginIp);
            loginLog.setLoginLocation(getLocationByIp(loginIp));
            
            // 保存14个设备信息字段
            loginLog.setDeviceModel(deviceModel);

            loginLog.setSystemVersion(systemVersion);

            
            loginLog.setLoginResult(loginResult);
            loginLog.setFailReason(failReason);
            
            loginLogService.recordLoginLog(loginLog);
        } catch (Exception e) {
            // 日志记录失败不影响登录流程
            log.error("记录登录日志失败", e);
        }
    }
    
    /**
     * 根据IP获取地理位置（可选功能，暂时返回空）
     */
    private String getLocationByIp(String ip) {
        // TODO: 可以调用第三方IP定位API或使用离线IP库
        // 暂时返回空，后续可以扩展
        return null;
    }
    
    /**
     * 根据token获取用户信息
     */
    @Override
    public WechatUser getUserByToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        
        try {
            // 从token中获取userId
            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                return null;
            }
            
            // 查询用户信息
            WechatUser user = wechatUserMapper.selectWechatUserByUserId(userId);
            return user;
        } catch (Exception e) {
            log.error("根据token获取用户信息失败", e);
            return null;
        }
    }
    
    /**
     * 刷新token
     */
    @Override
    public String refreshToken(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new RuntimeException("token不能为空");
        }
        
        try {
            // 验证token
            if (jwtTokenUtil.isTokenExpired(token)) {
                throw new RuntimeException("token已过期");
            }
            
            // 刷新token
            String newToken = jwtTokenUtil.refreshToken(token);
            return newToken;
        } catch (Exception e) {
            log.error("刷新token失败", e);
            throw new RuntimeException("刷新token失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户信息
     */
    @Override
    @Transactional
    public int updateUserInfo(Long userId, String nickName, String avatarUrl, 
                              Integer gender, String phone) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        WechatUser user = wechatUserMapper.selectWechatUserByUserId(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 更新用户信息
        if (StringUtils.isNotEmpty(nickName)) {
            user.setNickName(nickName);
        }
        if (StringUtils.isNotEmpty(avatarUrl)) {
            user.setAvatarUrl(avatarUrl);
        }
        if (gender != null) {
            user.setGender(gender);
        }
        if (StringUtils.isNotEmpty(phone)) {
            user.setPhone(phone);
        }
        user.setUpdateTime(new Date());
        
        return wechatUserMapper.updateWechatUser(user);
    }
    
    /**
     * 验证token是否有效
     */
    @Override
    public boolean validateToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        
        try {
            // 检查token是否过期
            if (jwtTokenUtil.isTokenExpired(token)) {
                return false;
            }
            
            // 从token中获取userId
            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                return false;
            }
            
            // 验证token
            return jwtTokenUtil.validateToken(token, userId);
        } catch (Exception e) {
            log.error("验证token失败", e);
            return false;
        }
    }
    
    /**
     * 登出
     */
    @Override
    public void logout(Long userId) {
        // 微信小程序登出通常不需要后端处理
        // 前端只需要清除本地存储的token即可
        // 这里可以记录登出日志或做其他业务处理
        log.info("用户登出: userId={}", userId);
    }
}
