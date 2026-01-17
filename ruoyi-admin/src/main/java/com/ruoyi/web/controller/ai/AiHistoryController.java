package com.ruoyi.web.controller.ai;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.annotation.RequiresAuth;
import com.ruoyi.web.domain.AiDetectionRecord;
import com.ruoyi.web.service.IAiHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * AI检测历史记录Controller
 * 
 * @author ruoyi
 */
@RequiresAuth
@RestController
@RequestMapping("/ai/history")
public class AiHistoryController extends BaseController {
    
    @Autowired
    private IAiHistoryService aiHistoryService;

    /**
     * 查询检测记录列表
     */
    @GetMapping("/list")
    public TableDataInfo list(AiDetectionRecord record, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        record.setUserId(userId);
        startPage();
        List<AiDetectionRecord> list = aiHistoryService.selectRecordList(record);
        return getDataTable(list);
    }

    /**
     * 获取检测记录详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        // 先查询记录
        AiDetectionRecord record = aiHistoryService.selectRecordById(id);
        if (record == null) {
            return AjaxResult.error("记录不存在");
        }
        
        // 验证记录是否属于当前用户
        if (!record.getUserId().equals(userId)) {
            return AjaxResult.error("无权访问该记录");
        }
        
        return AjaxResult.success(record);
    }

    /**
     * 删除检测记录
     */
    @Log(title = "AI检测记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        // 循环验证并删除，确保只删除属于当前用户的记录
        int rows = 0;
        for (Long id : ids) {
            AiDetectionRecord record = aiHistoryService.selectRecordById(id);
            if (record != null && record.getUserId().equals(userId)) {
                int result = aiHistoryService.deleteRecordById(id);
                if (result > 0) {
                    rows++;
                }
            }
        }
        
        return toAjax(rows);
    }

    /**
     * 导出检测记录
     */
    @Log(title = "AI检测记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AiDetectionRecord record, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        record.setUserId(userId);
        List<AiDetectionRecord> list = aiHistoryService.selectRecordList(record);
        ExcelUtil<AiDetectionRecord> util = new ExcelUtil<>(AiDetectionRecord.class);
        util.exportExcel(response, list, "检测记录数据");
    }
}
