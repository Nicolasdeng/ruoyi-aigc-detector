package com.ruoyi.web.controller.paper;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.web.domain.PaperDetectionRecord;
import com.ruoyi.web.domain.PaperParagraphDetail;
import com.ruoyi.web.service.IPaperDetectionService;
import com.ruoyi.web.service.paper.ISynonymService;
import com.ruoyi.web.service.paper.ISentenceTransformService;
import com.ruoyi.web.service.paper.IParagraphOptimizerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 论文检测Controller
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
@RestController
@RequestMapping("/paper/detection")
public class PaperDetectionController extends BaseController
{
    @Autowired
    private IPaperDetectionService paperDetectionService;

    @Autowired
    private ISynonymService synonymService;

    @Autowired
    private ISentenceTransformService sentenceTransformService;

    @Autowired
    private IParagraphOptimizerService paragraphOptimizerService;

    /**
     * 提交论文检测
     */
    @PreAuthorize("@ss.hasPermi('paper:detection:submit')")
    @Log(title = "论文检测", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody Map<String, String> params)
    {
        String title = params.get("title");
        String content = params.get("content");
        
        if (title == null || title.trim().isEmpty()) {
            return error("论文标题不能为空");
        }
        
        if (content == null || content.trim().isEmpty()) {
            return error("论文内容不能为空");
        }
        
        if (content.length() < 100) {
            return error("论文内容过短，至少需要100字");
        }
        
        try {
            Long userId = SecurityUtils.getUserId();
            Long detectionId = paperDetectionService.submitDetection(title, content, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("detectionId", detectionId);
            result.put("message", "检测提交成功");
            
            return success(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 查询检测结果
     */
    @PreAuthorize("@ss.hasPermi('paper:detection:query')")
    @GetMapping("/result/{id}")
    public AjaxResult getResult(@PathVariable("id") Long id)
    {
        PaperDetectionRecord record = paperDetectionService.getDetectionRecord(id);
        if (record == null) {
            return error("检测记录不存在");
        }
        
        // 检查权限：只能查看自己的检测记录
        if (!record.getUserId().equals(SecurityUtils.getUserId())) {
            return error("无权查看此检测记录");
        }
        
        return success(record);
    }

    /**
     * 查询段落详情
     */
    @PreAuthorize("@ss.hasPermi('paper:detection:query')")
    @GetMapping("/paragraphs/{detectionId}")
    public AjaxResult getParagraphs(@PathVariable("detectionId") Long detectionId)
    {
        // 验证权限
        PaperDetectionRecord record = paperDetectionService.getDetectionRecord(detectionId);
        if (record == null) {
            return error("检测记录不存在");
        }
        
        if (!record.getUserId().equals(SecurityUtils.getUserId())) {
            return error("无权查看此检测记录");
        }
        
        List<PaperParagraphDetail> details = paperDetectionService.getParagraphDetails(detectionId);
        return success(details);
    }

    /**
     * 查询高风险段落
     */
    @PreAuthorize("@ss.hasPermi('paper:detection:query')")
    @GetMapping("/highRisk/{detectionId}")
    public AjaxResult getHighRiskParagraphs(@PathVariable("detectionId") Long detectionId)
    {
        // 验证权限
        PaperDetectionRecord record = paperDetectionService.getDetectionRecord(detectionId);
        if (record == null) {
            return error("检测记录不存在");
        }
        
        if (!record.getUserId().equals(SecurityUtils.getUserId())) {
            return error("无权查看此检测记录");
        }
        
        List<PaperParagraphDetail> details = paperDetectionService.getHighRiskParagraphs(detectionId);
        return success(details);
    }

    /**
     * 获取修改建议
     */
    @PreAuthorize("@ss.hasPermi('paper:detection:query')")
    @GetMapping("/suggestions/{detectionId}")
    public AjaxResult getSuggestions(@PathVariable("detectionId") Long detectionId)
    {
        // 验证权限
        PaperDetectionRecord record = paperDetectionService.getDetectionRecord(detectionId);
        if (record == null) {
            return error("检测记录不存在");
        }
        
        if (!record.getUserId().equals(SecurityUtils.getUserId())) {
            return error("无权查看此检测记录");
        }
        
        Map<String, Object> suggestions = paperDetectionService.getSuggestions(detectionId);
        return success(suggestions);
    }

    /**
     * 查询检测历史
     */
    @PreAuthorize("@ss.hasPermi('paper:detection:list')")
    @GetMapping("/history")
    public TableDataInfo getHistory()
    {
        Long userId = SecurityUtils.getUserId();
        List<PaperDetectionRecord> list = paperDetectionService.getUserDetectionHistory(userId);
        return getDataTable(list);
    }

    /**
     * 查询检测记录列表
     */
    @PreAuthorize("@ss.hasPermi('paper:detection:list')")
    @GetMapping("/list")
    public TableDataInfo list(PaperDetectionRecord paperDetectionRecord) {
        Long currentUserId = SecurityUtils.getUserId();

        if (currentUserId != null && !SecurityUtils.isAdmin(currentUserId)) {
            paperDetectionRecord.setUserId(currentUserId);
        }

        startPage();
        List<PaperDetectionRecord> list = paperDetectionService.selectPaperDetectionRecordList(paperDetectionRecord);
        return getDataTable(list);
    }

    /**
     * 删除检测记录
     */
    @PreAuthorize("@ss.hasPermi('paper:detection:remove')")
    @Log(title = "论文检测", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        // 验证权限：只能删除自己的记录
        for (Long id : ids) {
            PaperDetectionRecord record = paperDetectionService.getDetectionRecord(id);
            if (record != null && !record.getUserId().equals(SecurityUtils.getUserId())) {
                return error("无权删除其他用户的检测记录");
            }
        }
        
        return toAjax(paperDetectionService.deletePaperDetectionRecordByIds(ids));
    }

    /**
     * 获取检测详情（包含记录和段落）
     */
    @PreAuthorize("@ss.hasPermi('paper:detection:query')")
    @GetMapping("/detail/{id}")
    public AjaxResult getDetail(@PathVariable("id") Long id) {
        PaperDetectionRecord record = paperDetectionService.getDetectionRecord(id);
        if (record == null) {
            return error("检测记录不存在");
        }

        // 检查权限：非管理员且不是本人记录则禁止访问
        Long currentUserId = SecurityUtils.getUserId();
        if (!record.getUserId().equals(currentUserId) && !SecurityUtils.isAdmin(currentUserId)) {
            return error("无权查看此检测记录");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("paragraphs", paperDetectionService.getParagraphDetails(id));
        result.put("highRiskParagraphs", paperDetectionService.getHighRiskParagraphs(id));

        return success(result);
    }

    /**
     * 智能优化论文内容
     */
    @PreAuthorize("@ss.hasPermi('paper:detection:optimize')")
    @PostMapping("/optimize/{detectionId}")
    public AjaxResult optimize(@PathVariable Long detectionId, @RequestBody Map<String, Object> params)
    {
        // 验证权限
        PaperDetectionRecord record = paperDetectionService.getDetectionRecord(detectionId);
        if (record == null) {
            return error("检测记录不存在");
        }
        
        if (!record.getUserId().equals(SecurityUtils.getUserId())) {
            return error("无权优化此检测记录");
        }
        
        try {
            String content = record.getContent();
            double optimizeLevel = params.containsKey("level") ? 
                ((Number) params.get("level")).doubleValue() : 0.3;
            
            // 应用同义词替换
            String optimized = synonymService.autoOptimize(content, optimizeLevel);
            
            // 应用句式变换
            optimized = sentenceTransformService.autoTransform(optimized, optimizeLevel * 0.5);
            
            Map<String, Object> result = new HashMap<>();
            result.put("original", content);
            result.put("optimized", optimized);
            result.put("optimizeLevel", optimizeLevel);
            
            return success(result);
        } catch (Exception e) {
            return error("优化失败: " + e.getMessage());
        }
    }

    /**
     * 获取优化预览
     */
    @PreAuthorize("@ss.hasPermi('paper:detection:query')")
    @GetMapping("/preview/{detectionId}")
    public AjaxResult preview(@PathVariable Long detectionId)
    {
        PaperDetectionRecord record = paperDetectionService.getDetectionRecord(detectionId);
        if (record == null) {
            return error("检测记录不存在");
        }
        
        if (!record.getUserId().equals(SecurityUtils.getUserId())) {
            return error("无权查看此检测记录");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        
        // 获取段落详情和优化建议
        List<PaperParagraphDetail> paragraphs = paperDetectionService.getParagraphDetails(detectionId);
        List<Map<String, Object>> optimizedParagraphs = new java.util.ArrayList<>();
        
        for (PaperParagraphDetail para : paragraphs) {
            Map<String, Object> item = new HashMap<>();
            item.put("original", para.getParagraphContent());
            item.put("aiRisk", para.getAiRisk());
            
            // 生成优化建议
            List<String> suggestions = paragraphOptimizerService.analyzeAndSuggest(para.getParagraphContent());
            item.put("suggestions", suggestions);
            
            // 生成优化后文本
            String optimized = synonymService.autoOptimize(para.getParagraphContent(), 0.3);
            item.put("optimized", optimized);
            
            optimizedParagraphs.add(item);
        }
        
        result.put("paragraphs", optimizedParagraphs);
        
        return success(result);
    }

    /**
     * 获取同义词建议
     */
    @PreAuthorize("@ss.hasPermi('paper:detection:query')")
    @PostMapping("/synonyms")
    public AjaxResult getSynonyms(@RequestBody Map<String, String> params)
    {
        String text = params.get("text");
        if (text == null || text.trim().isEmpty()) {
            return error("文本内容不能为空");
        }
        
        Map<String, List<String>> suggestions = synonymService.getSynonymSuggestions(text);
        return success(suggestions);
    }

    /**
     * 获取句式变换建议
     */
    @PreAuthorize("@ss.hasPermi('paper:detection:query')")
    @PostMapping("/transform")
    public AjaxResult transform(@RequestBody Map<String, String> params)
    {
        String sentence = params.get("sentence");
        if (sentence == null || sentence.trim().isEmpty()) {
            return error("句子内容不能为空");
        }
        
        List<String> transformations = sentenceTransformService.transformSentence(sentence);
        return success(transformations);
    }

    /**
     * 分析段落
     */
    @PreAuthorize("@ss.hasPermi('paper:detection:query')")
    @PostMapping("/analyzeParagraph")
    public AjaxResult analyzeParagraph(@RequestBody Map<String, String> params)
    {
        String paragraph = params.get("paragraph");
        if (paragraph == null || paragraph.trim().isEmpty()) {
            return error("段落内容不能为空");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("suggestions", paragraphOptimizerService.analyzeAndSuggest(paragraph));
        result.put("score", paragraphOptimizerService.calculateScore(paragraph));
        result.put("lengthAnalysis", paragraphOptimizerService.checkLength(paragraph));
        result.put("sentenceAnalysis", paragraphOptimizerService.analyzeSentenceLength(paragraph));
        result.put("coherenceAnalysis", paragraphOptimizerService.checkCoherence(paragraph));
        result.put("repetitiveWords", paragraphOptimizerService.detectRepetitiveWords(paragraph));
        
        return success(result);
    }
}
