package com.ruoyi.web.controller.paper;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.annotation.RequiresAuth;
import com.ruoyi.web.domain.PaperDetectionRecord;
import com.ruoyi.web.domain.PaperParagraphDetail;
import com.ruoyi.web.service.IPaperDetectionService;
import com.ruoyi.web.service.IAiImageDetectionService;
import com.ruoyi.web.service.paper.ISynonymService;
import com.ruoyi.web.service.paper.ISentenceTransformService;
import com.ruoyi.web.service.paper.IParagraphOptimizerService;
import com.ruoyi.web.domain.AiDetectionRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 论文检测Controller
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
@RequiresAuth
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

    @Autowired
    private IAiImageDetectionService aiImageDetectionService;

    /**
     * 验证用户对检测记录的访问权限
     * @param detectionId 检测记录ID
     * @param userId 当前用户ID
     * @return 检测记录，如果无权访问则返回null
     */
    private PaperDetectionRecord validateAccess(Long detectionId, Long userId) {
        PaperDetectionRecord record = paperDetectionService.getDetectionRecord(detectionId);
        if (record == null || !record.getUserId().equals(userId)) {
            return null;
        }
        return record;
    }

    /**
     * 提交论文检测
     */
    @Log(title = "论文检测", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody Map<String, String> params, HttpServletRequest request)
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
            Long userId = (Long) request.getAttribute("userId");
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
    @GetMapping("/result/{id}")
    public AjaxResult getResult(@PathVariable("id") Long id, HttpServletRequest request)
    {
        Long userId = (Long) request.getAttribute("userId");
        PaperDetectionRecord record = validateAccess(id, userId);
        if (record == null) {
            return error("检测记录不存在或无权访问");
        }

        return success(record);
    }

    /**
     * 查询段落详情
     */
    @GetMapping("/paragraphs/{detectionId}")
    public AjaxResult getParagraphs(@PathVariable("detectionId") Long detectionId, HttpServletRequest request)
    {
        Long userId = (Long) request.getAttribute("userId");
        PaperDetectionRecord record = validateAccess(detectionId, userId);
        if (record == null) {
            return error("检测记录不存在或无权访问");
        }

        List<PaperParagraphDetail> details = paperDetectionService.getParagraphDetails(detectionId);
        return success(details);
    }

    /**
     * 查询高风险段落
     */
    @GetMapping("/highRisk/{detectionId}")
    public AjaxResult getHighRiskParagraphs(@PathVariable("detectionId") Long detectionId, HttpServletRequest request)
    {
        Long userId = (Long) request.getAttribute("userId");
        PaperDetectionRecord record = validateAccess(detectionId, userId);
        if (record == null) {
            return error("检测记录不存在或无权访问");
        }

        List<PaperParagraphDetail> details = paperDetectionService.getHighRiskParagraphs(detectionId);
        return success(details);
    }

    /**
     * 获取修改建议
     */
    @GetMapping("/suggestions/{detectionId}")
    public AjaxResult getSuggestions(@PathVariable("detectionId") Long detectionId, HttpServletRequest request)
    {
        Long userId = (Long) request.getAttribute("userId");
        PaperDetectionRecord record = validateAccess(detectionId, userId);
        if (record == null) {
            return error("检测记录不存在或无权访问");
        }

        Map<String, Object> suggestions = paperDetectionService.getSuggestions(detectionId);
        return success(suggestions);
    }

    /**
     * 查询检测历史
     */
    @GetMapping("/history")
    public TableDataInfo getHistory(HttpServletRequest request)
    {
        Long userId = (Long) request.getAttribute("userId");
        List<PaperDetectionRecord> list = paperDetectionService.getUserDetectionHistory(userId);
        return getDataTable(list);
    }

    /**
     * 查询检测记录列表
     */
    @GetMapping("/list")
    public TableDataInfo list(PaperDetectionRecord paperDetectionRecord, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        paperDetectionRecord.setUserId(currentUserId);

        startPage();
        List<PaperDetectionRecord> list = paperDetectionService.selectPaperDetectionRecordList(paperDetectionRecord);
        return getDataTable(list);
    }

    /**
     * 删除检测记录
     */
    @Log(title = "论文检测", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids, HttpServletRequest request)
    {
        // 验证权限：只能删除自己的记录
        Long userId = (Long) request.getAttribute("userId");
        for (Long id : ids) {
            PaperDetectionRecord record = paperDetectionService.getDetectionRecord(id);
            if (record != null && !record.getUserId().equals(userId)) {
                return error("无权删除其他用户的检测记录");
            }
        }
        
        return toAjax(paperDetectionService.deletePaperDetectionRecordByIds(ids));
    }

    /**
     * 获取检测详情（包含记录和段落）
     */
    @GetMapping("/detail/{id}")
    public AjaxResult getDetail(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PaperDetectionRecord record = validateAccess(id, userId);
        if (record == null) {
            return error("检测记录不存在或无权访问");
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
    @PostMapping("/optimize/{detectionId}")
    public AjaxResult optimize(@PathVariable Long detectionId, @RequestBody Map<String, Object> params, HttpServletRequest request)
    {
        Long userId = (Long) request.getAttribute("userId");
        PaperDetectionRecord record = validateAccess(detectionId, userId);
        if (record == null) {
            return error("检测记录不存在或无权访问");
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
            logger.error("优化失败", e);
            return error("优化失败: " + e.getMessage());
        }
    }

    /**
     * 获取优化预览
     */
    @GetMapping("/preview/{detectionId}")
    public AjaxResult preview(@PathVariable Long detectionId, HttpServletRequest request)
    {
        Long userId = (Long) request.getAttribute("userId");
        PaperDetectionRecord record = validateAccess(detectionId, userId);
        if (record == null) {
            return error("检测记录不存在或无权访问");
        }

        try {
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
        } catch (Exception e) {
            logger.error("获取预览失败", e);
            return error("获取预览失败: " + e.getMessage());
        }
    }

    /**
     * 获取同义词建议
     */
    @PostMapping("/synonyms")
    public AjaxResult getSynonyms(@RequestBody Map<String, String> params)
    {
        String text = params.get("text");
        if (text == null || text.trim().isEmpty()) {
            return error("文本内容不能为空");
        }

        try {
            Map<String, List<String>> suggestions = synonymService.getSynonymSuggestions(text);
            return success(suggestions);
        } catch (Exception e) {
            logger.error("获取同义词建议失败", e);
            return error("获取同义词建议失败: " + e.getMessage());
        }
    }

    /**
     * 获取句式变换建议
     */
    @PostMapping("/transform")
    public AjaxResult transform(@RequestBody Map<String, String> params)
    {
        String sentence = params.get("sentence");
        if (sentence == null || sentence.trim().isEmpty()) {
            return error("句子内容不能为空");
        }

        try {
            List<String> transformations = sentenceTransformService.transformSentence(sentence);
            return success(transformations);
        } catch (Exception e) {
            logger.error("获取句式变换建议失败", e);
            return error("获取句式变换建议失败: " + e.getMessage());
        }
    }

    /**
     * 分析段落
     */
    @PostMapping("/analyzeParagraph")
    public AjaxResult analyzeParagraph(@RequestBody Map<String, String> params)
    {
        String paragraph = params.get("paragraph");
        if (paragraph == null || paragraph.trim().isEmpty()) {
            return error("段落内容不能为空");
        }

        try {
            Map<String, Object> result = new HashMap<>();
            result.put("suggestions", paragraphOptimizerService.analyzeAndSuggest(paragraph));
            result.put("score", paragraphOptimizerService.calculateScore(paragraph));
            result.put("lengthAnalysis", paragraphOptimizerService.checkLength(paragraph));
            result.put("sentenceAnalysis", paragraphOptimizerService.analyzeSentenceLength(paragraph));
            result.put("coherenceAnalysis", paragraphOptimizerService.checkCoherence(paragraph));
            result.put("repetitiveWords", paragraphOptimizerService.detectRepetitiveWords(paragraph));

            return success(result);
        } catch (Exception e) {
            logger.error("分析段落失败", e);
            return error("分析段落失败: " + e.getMessage());
        }
    }

    /**
     * 检测论文中的图片
     */
    @Log(title = "论文图片检测", businessType = BusinessType.INSERT)
    @PostMapping("/detectImage/{detectionId}")
    public AjaxResult detectImage(
            @PathVariable Long detectionId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request)
    {
        Long userId = (Long) request.getAttribute("userId");
        PaperDetectionRecord record = validateAccess(detectionId, userId);
        if (record == null) {
            return error("检测记录不存在或无权访问");
        }

        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                return error("请选择要上传的图片");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return error("只支持图片文件");
            }

            // 验证文件大小 (限制10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return error("图片大小不能超过10MB");
            }

            // 执行图片检测
            AiDetectionRecord imageRecord = aiImageDetectionService.detectImage(file, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("detectionId", detectionId);
            result.put("imageDetection", imageRecord);
            result.put("message", "图片检测完成");

            return success(result);
        } catch (Exception e) {
            logger.error("图片检测失败", e);
            return error("图片检测失败: " + e.getMessage());
        }
    }

    /**
     * 通过URL检测论文中的图片
     */
    @Log(title = "论文图片URL检测", businessType = BusinessType.INSERT)
    @PostMapping("/detectImageByUrl/{detectionId}")
    public AjaxResult detectImageByUrl(
            @PathVariable Long detectionId,
            @RequestBody Map<String, String> params,
            HttpServletRequest request)
    {
        Long userId = (Long) request.getAttribute("userId");
        PaperDetectionRecord record = validateAccess(detectionId, userId);
        if (record == null) {
            return error("检测记录不存在或无权访问");
        }

        String imageUrl = params.get("imageUrl");
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return error("图片URL不能为空");
        }

        // 简单的URL格式验证
        if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
            return error("请提供有效的图片URL（需以http://或https://开头）");
        }

        try {
            AiDetectionRecord imageRecord = aiImageDetectionService.detectImageByUrl(imageUrl, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("detectionId", detectionId);
            result.put("imageDetection", imageRecord);
            result.put("message", "图片检测完成");

            return success(result);
        } catch (Exception e) {
            logger.error("图片URL检测失败", e);
            return error("图片URL检测失败: " + e.getMessage());
        }
    }

    /**
     * 批量检测论文中的多张图片
     */
    @Log(title = "论文批量图片检测", businessType = BusinessType.INSERT)
    @PostMapping("/detectImages/{detectionId}")
    public AjaxResult detectImages(
            @PathVariable Long detectionId,
            @RequestParam("files") MultipartFile[] files,
            HttpServletRequest request)
    {
        Long userId = (Long) request.getAttribute("userId");
        PaperDetectionRecord record = validateAccess(detectionId, userId);
        if (record == null) {
            return error("检测记录不存在或无权访问");
        }

        if (files == null || files.length == 0) {
            return error("请至少选择一张图片");
        }

        if (files.length > 10) {
            return error("一次最多检测10张图片");
        }

        try {
            List<AiDetectionRecord> imageRecords = new java.util.ArrayList<>();
            List<String> errors = new java.util.ArrayList<>();

            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                try {
                    // 验证文件
                    if (file.isEmpty()) {
                        errors.add("第" + (i + 1) + "张图片为空");
                        continue;
                    }

                    String contentType = file.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        errors.add("第" + (i + 1) + "个文件不是图片");
                        continue;
                    }

                    if (file.getSize() > 10 * 1024 * 1024) {
                        errors.add("第" + (i + 1) + "张图片超过10MB");
                        continue;
                    }

                    AiDetectionRecord imageRecord = aiImageDetectionService.detectImage(file, userId);
                    imageRecords.add(imageRecord);
                } catch (Exception e) {
                    logger.error("检测第" + (i + 1) + "张图片失败", e);
                    errors.add("第" + (i + 1) + "张图片检测失败: " + e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("detectionId", detectionId);
            result.put("imageDetections", imageRecords);
            result.put("successCount", imageRecords.size());
            result.put("failCount", errors.size());
            result.put("errors", errors);

            return success(result);
        } catch (Exception e) {
            logger.error("批量图片检测失败", e);
            return error("批量图片检测失败: " + e.getMessage());
        }
    }

    /**
     * 批量优化多个检测记录
     */
    @Log(title = "批量优化论文", businessType = BusinessType.UPDATE)
    @PostMapping("/batchOptimize")
    public AjaxResult batchOptimize(@RequestBody Map<String, Object> params, HttpServletRequest request)
    {
        Long userId = (Long) request.getAttribute("userId");

        @SuppressWarnings("unchecked")
        List<Long> detectionIds = (List<Long>) params.get("detectionIds");
        if (detectionIds == null || detectionIds.isEmpty()) {
            return error("请选择要优化的检测记录");
        }

        if (detectionIds.size() > 20) {
            return error("一次最多优化20条记录");
        }

        double optimizeLevel = params.containsKey("level") ?
            ((Number) params.get("level")).doubleValue() : 0.3;

        try {
            List<Map<String, Object>> results = new java.util.ArrayList<>();
            List<String> errors = new java.util.ArrayList<>();

            for (Long detectionId : detectionIds) {
                try {
                    PaperDetectionRecord record = validateAccess(detectionId, userId);
                    if (record == null) {
                        errors.add("记录ID " + detectionId + ": 不存在或无权访问");
                        continue;
                    }

                    String content = record.getContent();
                    String optimized = synonymService.autoOptimize(content, optimizeLevel);
                    optimized = sentenceTransformService.autoTransform(optimized, optimizeLevel * 0.5);

                    Map<String, Object> item = new HashMap<>();
                    item.put("detectionId", detectionId);
                    item.put("title", record.getTitle());
                    item.put("original", content);
                    item.put("optimized", optimized);

                    results.add(item);
                } catch (Exception e) {
                    logger.error("优化记录ID " + detectionId + " 失败", e);
                    errors.add("记录ID " + detectionId + ": " + e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("results", results);
            result.put("successCount", results.size());
            result.put("failCount", errors.size());
            result.put("errors", errors);
            result.put("optimizeLevel", optimizeLevel);

            return success(result);
        } catch (Exception e) {
            logger.error("批量优化失败", e);
            return error("批量优化失败: " + e.getMessage());
        }
    }
}
