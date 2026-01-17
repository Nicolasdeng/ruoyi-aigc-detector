package com.ruoyi.web.controller.ai;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.annotation.RequiresAuth;
import com.ruoyi.web.service.IAiStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * AI检测统计信息Controller
 * 
 * @author ruoyi
 */
@RequiresAuth
@RestController
@RequestMapping("/ai/statistics")
public class AiStatisticsController extends BaseController {
    
    @Autowired
    private IAiStatisticsService aiStatisticsService;

    /**
     * 获取统计信息汇总
     */
    @GetMapping("/summary")
    public AjaxResult getSummary(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return AjaxResult.success(aiStatisticsService.getStatistics(userId));
    }

    /**
     * 获取今日统计信息
     */
    @GetMapping("/today")
    public AjaxResult getToday(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return AjaxResult.success(aiStatisticsService.getTodayStatistics(userId));
    }

    /**
     * 获取趋势统计信息
     */
    @GetMapping("/trend")
    public AjaxResult getTrend(@RequestParam(defaultValue = "7") int days, HttpServletRequest request) {
        if (days < 1 || days > 90) {
            return AjaxResult.error("统计天数范围应在1-90天之间");
        }
        Long userId = (Long) request.getAttribute("userId");
        return AjaxResult.success(aiStatisticsService.getTrendStatistics(userId, days));
    }
}
