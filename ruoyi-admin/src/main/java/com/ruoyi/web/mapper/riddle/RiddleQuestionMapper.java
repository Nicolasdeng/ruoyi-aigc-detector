package com.ruoyi.web.mapper.riddle;

import java.util.List;
import com.ruoyi.web.domain.riddle.RiddleQuestion;
import org.apache.ibatis.annotations.Param;

/**
 * 脑筋急转弯题目Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-26
 */
public interface RiddleQuestionMapper 
{
    /**
     * 查询脑筋急转弯题目
     * 
     * @param id 脑筋急转弯题目主键
     * @return 脑筋急转弯题目
     */
    public RiddleQuestion selectRiddleQuestionById(Long id);

    /**
     * 查询脑筋急转弯题目列表
     * 
     * @param riddleQuestion 脑筋急转弯题目
     * @return 脑筋急转弯题目集合
     */
    public List<RiddleQuestion> selectRiddleQuestionList(RiddleQuestion riddleQuestion);

    /**
     * 新增脑筋急转弯题目
     * 
     * @param riddleQuestion 脑筋急转弯题目
     * @return 结果
     */
    public int insertRiddleQuestion(RiddleQuestion riddleQuestion);

    /**
     * 修改脑筋急转弯题目
     * 
     * @param riddleQuestion 脑筋急转弯题目
     * @return 结果
     */
    public int updateRiddleQuestion(RiddleQuestion riddleQuestion);

    /**
     * 删除脑筋急转弯题目
     * 
     * @param id 脑筋急转弯题目主键
     * @return 结果
     */
    public int deleteRiddleQuestionById(Long id);

    /**
     * 批量删除脑筋急转弯题目
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRiddleQuestionByIds(Long[] ids);

    /**
     * 随机获取一道题目
     * 
     * @param category 分类（可选）
     * @param difficulty 难度（可选）
     * @return 题目
     */
    public RiddleQuestion selectRandomQuestion(@Param("category") String category, 
                                               @Param("difficulty") String difficulty);

    /**
     * 增加题目查看次数
     * 
     * @param id 题目ID
     * @return 结果
     */
    public int increaseViews(Long id);

    /**
     * 增加题目点赞数
     * 
     * @param id 题目ID
     * @return 结果
     */
    public int increaseLikes(Long id);
}
