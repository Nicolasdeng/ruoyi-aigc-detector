package com.ruoyi.web.controller.agreement;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.web.annotation.RateLimit;
import com.ruoyi.web.domain.SysUserAgreement;
import com.ruoyi.web.domain.SysUserAgreementRecord;
import com.ruoyi.web.service.ISysUserAgreementRecordService;
import com.ruoyi.web.service.ISysUserAgreementService;
import com.ruoyi.web.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户协议Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/agreement")
public class SysUserAgreementController extends BaseController {
    
    @Autowired
    private ISysUserAgreementService agreementService;
    
    @Autowired
    private ISysUserAgreementRecordService recordService;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    /**
     * 获取最新生效的协议（根据类型）
     * 用于用户端展示最新版本的协议内容
     */
    @GetMapping("/latest/{agreementType}")
    @RateLimit(key = "agreement:latest", time = 60, count = 100)
    public AjaxResult getLatestAgreement(@PathVariable String agreementType) {
        SysUserAgreement agreement = agreementService.getLatestAgreementByType(Integer.valueOf(agreementType));
        if (agreement == null) {
            return AjaxResult.error("协议不存在");
        }
        return AjaxResult.success(agreement);
    }
    
    /**
     * 获取协议详情
     */
    @GetMapping("/{agreementId}")
    @RateLimit(key = "agreement:detail", time = 60, count = 100)
    public AjaxResult getAgreementDetail(@PathVariable Long agreementId) {
        SysUserAgreement agreement = agreementService.getAgreementById(agreementId);
        if (agreement == null) {
            return AjaxResult.error("协议不存在");
        }
        return AjaxResult.success(agreement);
    }
    
    /**
     * 用户确认协议
     * 用于记录用户对协议的确认操作
     */
    @PostMapping("/confirm")
    @RateLimit(key = "agreement:confirm", time = 60, count = 10)
    public AjaxResult confirmAgreement(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 1. 从Token中获取用户ID
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return AjaxResult.error("未登录或Token无效");
        }
        token = token.substring(7);
        
        Long userId;
        try {
            userId = jwtTokenUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return AjaxResult.error("Token解析失败");
        }
        
        // 2. 获取协议ID
        Long agreementId = Long.valueOf(params.get("agreementId").toString());
        
        // 3. 获取IP地址和设备信息
        String ipAddress = getRemoteAddr(request);
        String deviceInfo = request.getHeader("User-Agent");
        
        // 4. 确认协议
        try {
            Long recordId = recordService.confirmAgreement(userId, agreementId, ipAddress, deviceInfo);
            if (recordId == null) {
                return AjaxResult.error("您已确认过该协议");
            }
            return AjaxResult.success("协议确认成功", recordId);
        } catch (Exception e) {
            return AjaxResult.error("协议确认失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查用户是否需要确认新协议
     * 用于登录后检查是否有新版本协议需要用户确认
     */
    @GetMapping("/check/required")
    @RateLimit(key = "agreement:check", time = 60, count = 50)
    public AjaxResult checkAgreementRequired(HttpServletRequest request) {
        // 1. 从Token中获取用户ID
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return AjaxResult.error("未登录或Token无效");
        }
        token = token.substring(7);
        
        Long userId;
        try {
            userId = jwtTokenUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return AjaxResult.error("Token解析失败");
        }
        
        // 2. 检查用户协议和隐私政策
        Map<String, Object> result = new HashMap<>();
        
        // 检查用户协议
        SysUserAgreement userAgreement = agreementService.getLatestAgreementByType(Integer.valueOf("user_agreement"));
        boolean needConfirmUserAgreement = false;
        if (userAgreement != null) {
            needConfirmUserAgreement = agreementService.needsUserConfirmation(
                userId, Integer.valueOf("user_agreement")
            );
        }
        
        // 检查隐私政策
        SysUserAgreement privacyPolicy = agreementService.getLatestAgreementByType(Integer.valueOf("privacy_policy"));
        boolean needConfirmPrivacyPolicy = false;
        if (privacyPolicy != null) {
            needConfirmPrivacyPolicy = agreementService.needsUserConfirmation(
                userId, Integer.valueOf("privacy_policy")
            );
        }
        
        result.put("needConfirm", needConfirmUserAgreement || needConfirmPrivacyPolicy);
        result.put("needConfirmUserAgreement", needConfirmUserAgreement);
        result.put("needConfirmPrivacyPolicy", needConfirmPrivacyPolicy);
        
        if (userAgreement != null) {
            result.put("userAgreement", userAgreement);
        }
        if (privacyPolicy != null) {
            result.put("privacyPolicy", privacyPolicy);
        }
        
        return AjaxResult.success(result);
    }
    
    /**
     * 查询用户的协议确认记录
     */
    @GetMapping("/records/my")
    @RateLimit(key = "agreement:records", time = 60, count = 20)
    public TableDataInfo getMyAgreementRecords(HttpServletRequest request) {
        // 1. 从Token中获取用户ID
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return getDataTable(null);
        }
        token = token.substring(7);
        
        Long userId;
        try {
            userId = jwtTokenUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return getDataTable(null);
        }
        
        // 2. 查询记录
        startPage();
        List<SysUserAgreementRecord> records = recordService.selectRecordsByUserId(userId);
        return getDataTable(records);
    }
    
    // ==================== 管理员接口 ====================
    
    /**
     * 查询协议列表（管理员）
     */
    @GetMapping("/admin/list")
    @RateLimit(key = "agreement:admin:list", time = 60, count = 30)
    public TableDataInfo listAgreements(SysUserAgreement agreement) {
        startPage();
        List<SysUserAgreement> list = agreementService.selectAgreementList(agreement);
        return getDataTable(list);
    }
    
    /**
     * 新增协议（管理员）
     */
    @PostMapping("/admin/add")
    @RateLimit(key = "agreement:admin:add", time = 60, count = 10)
    public AjaxResult addAgreement(@Validated @RequestBody SysUserAgreement agreement) {
        try {
            Long agreementId = Long.valueOf(agreementService.insertAgreement(agreement));
            return AjaxResult.success("协议新增成功", agreementId);
        } catch (Exception e) {
            return AjaxResult.error("协议新增失败：" + e.getMessage());
        }
    }
    
    /**
     * 修改协议（管理员）
     */
    @PutMapping("/admin/edit")
    @RateLimit(key = "agreement:admin:edit", time = 60, count = 10)
    public AjaxResult editAgreement(@Validated @RequestBody SysUserAgreement agreement) {
        try {
            int rows = agreementService.updateAgreement(agreement);
            return rows > 0 ? AjaxResult.success("协议修改成功") : AjaxResult.error("协议修改失败");
        } catch (Exception e) {
            return AjaxResult.error("协议修改失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除协议（管理员）
     */
    @DeleteMapping("/admin/delete/{agreementIds}")
    @RateLimit(key = "agreement:admin:delete", time = 60, count = 10)
    public AjaxResult deleteAgreement(@PathVariable Long[] agreementIds) {
        try {
            int rows = 0;
            for (Long agreementId : agreementIds) {
                int result = agreementService.deleteAgreementById(agreementId);
                if (result > 0) {
                    rows++;
                }
            }
            return rows > 0 ? AjaxResult.success("成功删除" + rows + "个协议") : AjaxResult.error("协议删除失败");
        } catch (Exception e) {
            return AjaxResult.error("协议删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 发布新版本协议（管理员）
     */
    @PostMapping("/admin/publish/{agreementId}")
    @RateLimit(key = "agreement:admin:publish", time = 60, count = 5)
    public AjaxResult publishAgreement(@PathVariable Long agreementId) {
        try {
            SysUserAgreement agreement = agreementService.getAgreementById(agreementId);
            if (agreement == null) {
                return AjaxResult.error("协议不存在");
            }
            int rows = agreementService.publishNewVersion(agreement);
            return rows > 0 ? AjaxResult.success("协议发布成功") : AjaxResult.error("协议发布失败");
        } catch (Exception e) {
            return AjaxResult.error("协议发布失败：" + e.getMessage());
        }
    }
    
    /**
     * 停用协议（管理员）
     */
    @PostMapping("/admin/disable/{agreementId}")
    @RateLimit(key = "agreement:admin:disable", time = 60, count = 5)
    public AjaxResult disableAgreement(@PathVariable Long agreementId) {
        try {
            // 先查询协议
            SysUserAgreement agreement = agreementService.getAgreementById(agreementId);
            if (agreement == null) {
                return AjaxResult.error("协议不存在");
            }
            
            // 设置状态为停用（0）
            agreement.setIsActive(0);
            
            // 更新协议
            int rows = agreementService.updateAgreement(agreement);
            return rows > 0 ? AjaxResult.success("协议停用成功") : AjaxResult.error("协议停用失败");
        } catch (Exception e) {
            return AjaxResult.error("协议停用失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询协议统计信息（管理员）
     */
    @GetMapping("/admin/statistics/{agreementId}")
    @RateLimit(key = "agreement:admin:statistics", time = 60, count = 20)
    public AjaxResult getAgreementStatistics(@PathVariable Long agreementId) {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 总确认人数
            int confirmedCount = recordService.countConfirmedUsers(agreementId);
            statistics.put("confirmedCount", confirmedCount);
            
            // 未确认用户列表（仅返回数量，避免数据量过大）
            List<Long> unconfirmedUsers = recordService.selectUnconfirmedUsers(agreementId);
            statistics.put("unconfirmedCount", unconfirmedUsers.size());
            
            // 协议详情
            SysUserAgreement agreement = agreementService.getAgreementById(agreementId);
            statistics.put("agreement", agreement);
            
            return AjaxResult.success(statistics);
        } catch (Exception e) {
            return AjaxResult.error("统计信息查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询协议确认记录列表（管理员）
     */
    @GetMapping("/admin/records")
    @RateLimit(key = "agreement:admin:records", time = 60, count = 20)
    public TableDataInfo listAgreementRecords(SysUserAgreementRecord record) {
        startPage();
        List<SysUserAgreementRecord> list = recordService.selectRecordList(record);
        return getDataTable(list);
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getRemoteAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
