package com.ruoyi.web.controller.riddle;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.annotation.Anonymous;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.web.domain.riddle.RiddleQuestion;
import com.ruoyi.web.domain.riddle.RiddleHistory;
import com.ruoyi.web.service.riddle.IRiddleService;


/**
 * 脑筋急转弯Controller
 * 
 * @author ruoyi
 * @date 2025-12-26
 */
@RestController
@RequestMapping("/riddle")
public class RiddleController extends BaseController
{
    @Autowired
    private IRiddleService riddleService;

    /**
     * 获取随机题目
     */
    @GetMapping("/random")
    public AjaxResult getRandomQuestion(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty)
    {
        RiddleQuestion question = riddleService.getRandomQuestion(category, difficulty);
        return success(question);
    }

    /**
     * 查看答案
     */
    @GetMapping("/answer/{questionId}")
    public AjaxResult viewAnswer(@PathVariable Long questionId)
    {
        RiddleQuestion question = riddleService.viewAnswer(questionId);
        return success(question);
    }

    /**
     * 提交答案
     */
    @Anonymous
    @PostMapping("/submit")
    public AjaxResult submitAnswer(@RequestBody Map<String, Object> params, HttpServletRequest request)
    {
        // 从request获取userId（已登录用户），未登录时为null
        Long userId = (Long) request.getAttribute("userId");
        Long questionId = Long.valueOf(params.get("questionId").toString());
        String userAnswer = params.get("userAnswer").toString();
        Integer timeSpent = params.get("timeSpent") != null ? 
            Integer.valueOf(params.get("timeSpent").toString()) : 0;
        
        Map<String, Object> result = riddleService.submitAnswer(userId, questionId, userAnswer, timeSpent);
        return success(result);
    }

    /**
     * 查询答题历史
     */
    @Anonymous
    @GetMapping("/history")
    public AjaxResult getUserHistory(HttpServletRequest request)
    {
        // 从request获取userId（已登录用户），未登录时为null
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            // 游客模式：返回空列表
            return success(new java.util.ArrayList<>());
        }
        
        List<RiddleHistory> list = riddleService.getUserHistory(userId);
        return success(list);
    }

    /**
     * 获取用户统计
     */
    @Anonymous
    @GetMapping("/statistics")
    public AjaxResult getUserStatistics(HttpServletRequest request)
    {
        // 从request获取userId（已登录用户），未登录时为null
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            // 游客模式：返回默认统计数据
            Map<String, Object> defaultStats = new java.util.HashMap<>();
            defaultStats.put("totalCount", 0);
            defaultStats.put("correctCount", 0);
            defaultStats.put("accuracy", 0.0);
            return success(defaultStats);
        }
        
        Map<String, Object> statistics = riddleService.getUserStatistics(userId);
        return success(statistics);
    }

    /**
     * 点赞题目
     */
    @PostMapping("/like/{questionId}")
    public AjaxResult likeQuestion(@PathVariable Long questionId)
    {
        return toAjax(riddleService.likeQuestion(questionId));
    }

    // ========== 以下是管理端接口 ==========

    /**
     * 查询题目列表
     */
    @PreAuthorize("@ss.hasPermi('riddle:question:list')")
    @GetMapping("/admin/list")
    public TableDataInfo list(RiddleQuestion riddleQuestion)
    {
        startPage();
        List<RiddleQuestion> list = riddleService.selectRiddleQuestionList(riddleQuestion);
        return getDataTable(list);
    }

    /**
     * 新增题目
     */
    @PreAuthorize("@ss.hasPermi('riddle:question:add')")
    @Log(title = "脑筋急转弯题目", businessType = BusinessType.INSERT)
    @PostMapping("/admin")
    public AjaxResult add(@RequestBody RiddleQuestion riddleQuestion)
    {
        return toAjax(riddleService.insertRiddleQuestion(riddleQuestion));
    }

    /**
     * 修改题目
     */
    @PreAuthorize("@ss.hasPermi('riddle:question:edit')")
    @Log(title = "脑筋急转弯题目", businessType = BusinessType.UPDATE)
    @PutMapping("/admin")
    public AjaxResult edit(@RequestBody RiddleQuestion riddleQuestion)
    {
        return toAjax(riddleService.updateRiddleQuestion(riddleQuestion));
    }

    /**
     * 删除题目
     */
    @PreAuthorize("@ss.hasPermi('riddle:question:remove')")
    @Log(title = "脑筋急转弯题目", businessType = BusinessType.DELETE)
	@DeleteMapping("/admin/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(riddleService.deleteRiddleQuestionByIds(ids));
    }
}
