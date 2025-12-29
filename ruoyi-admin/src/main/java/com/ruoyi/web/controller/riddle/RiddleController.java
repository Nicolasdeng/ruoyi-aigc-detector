package com.ruoyi.web.controller.riddle;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
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
import com.ruoyi.common.utils.SecurityUtils;

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
    @PostMapping("/submit")
    public AjaxResult submitAnswer(@RequestBody Map<String, Object> params)
    {
        Long userId = SecurityUtils.getUserId();
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
    @GetMapping("/history")
    public AjaxResult getUserHistory()
    {
        Long userId = SecurityUtils.getUserId();
        List<RiddleHistory> list = riddleService.getUserHistory(userId);
        return success(list);
    }

    /**
     * 获取用户统计
     */
    @GetMapping("/statistics")
    public AjaxResult getUserStatistics()
    {
        Long userId = SecurityUtils.getUserId();
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
