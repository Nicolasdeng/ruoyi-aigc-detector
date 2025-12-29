package com.ruoyi.web.controller.ai;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.service.IAiStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI检测统计信息Controller
 * 
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/ai/statistics")
public class AiStatisticsController extends BaseController {
    
    @Autowired
    private IAiStatisticsService aiStatisticsService;

    /**
     * 获取统计信息汇总
     */
    @Anonymous
    @GetMapping("/summary")
    public AjaxResult getSummary() {
        return AjaxResult.success(aiStatisticsService.getStatistics());
    }

    /**
     * 获取今日统计信息
     */
    @Anonymous
    @GetMapping("/today")
    public AjaxResult getToday() {
        return AjaxResult.success(aiStatisticsService.getTodayStatistics());
    }

    /**
     * 获取趋势统计信息
     */
    @Anonymous
    @GetMapping("/trend")
    public AjaxResult getTrend(@RequestParam(defaultValue = "7") int days) {
        if (days < 1 || days > 90) {
            return AjaxResult.error("统计天数范围应在1-90天之间");
        }
        return AjaxResult.success(aiStatisticsService.getTrendStatistics(days));
    }
}
