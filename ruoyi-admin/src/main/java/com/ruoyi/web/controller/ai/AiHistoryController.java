package com.ruoyi.web.controller.ai;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.AiDetectionRecord;
import com.ruoyi.web.service.IAiHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * AI检测历史记录Controller
 * 
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/ai/history")
public class AiHistoryController extends BaseController {
    
    @Autowired
    private IAiHistoryService aiHistoryService;

    /**
     * 查询检测记录列表
     */
    @Anonymous
    @GetMapping("/list")
    public TableDataInfo list(AiDetectionRecord record) {
        startPage();
        List<AiDetectionRecord> list = aiHistoryService.selectRecordList(record);
        return getDataTable(list);
    }

    /**
     * 获取检测记录详细信息
     */
    @Anonymous
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(aiHistoryService.selectRecordById(id));
    }

    /**
     * 删除检测记录
     */
    @Anonymous
    @Log(title = "AI检测记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(aiHistoryService.deleteRecordByIds(ids));
    }

    /**
     * 导出检测记录
     */
    @PreAuthorize("@ss.hasPermi('ai:detection:export')")
    @Log(title = "AI检测记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AiDetectionRecord record) {
        List<AiDetectionRecord> list = aiHistoryService.selectRecordList(record);
        ExcelUtil<AiDetectionRecord> util = new ExcelUtil<>(AiDetectionRecord.class);
        util.exportExcel(response, list, "检测记录数据");
    }
}
