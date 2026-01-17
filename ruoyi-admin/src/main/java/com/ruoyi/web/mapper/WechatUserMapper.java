package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.WechatUser;

/**
 * 微信小程序用户Mapper接口
 * 
 * @author ruoyi
 * @date 2026-01-11
 */
public interface WechatUserMapper 
{
    /**
     * 查询微信小程序用户
     * 
     * @param userId 微信小程序用户主键
     * @return 微信小程序用户
     */
    public WechatUser selectWechatUserByUserId(Long userId);

    /**
     * 根据openid查询微信用户
     * 
     * @param openid 微信openid
     * @return 微信小程序用户
     */
    public WechatUser selectWechatUserByOpenid(String openid);

    /**
     * 查询微信小程序用户列表
     * 
     * @param wechatUser 微信小程序用户
     * @return 微信小程序用户集合
     */
    public List<WechatUser> selectWechatUserList(WechatUser wechatUser);

    /**
     * 新增微信小程序用户
     * 
     * @param wechatUser 微信小程序用户
     * @return 结果
     */
    public int insertWechatUser(WechatUser wechatUser);

    /**
     * 修改微信小程序用户
     * 
     * @param wechatUser 微信小程序用户
     * @return 结果
     */
    public int updateWechatUser(WechatUser wechatUser);

    /**
     * 删除微信小程序用户
     * 
     * @param userId 微信小程序用户主键
     * @return 结果
     */
    public int deleteWechatUserByUserId(Long userId);

    /**
     * 批量删除微信小程序用户
     * 
     * @param userIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWechatUserByUserIds(Long[] userIds);

    /**
     * 更新用户登录信息
     * 
     * @param wechatUser 微信小程序用户
     * @return 结果
     */
    public int updateLoginInfo(WechatUser wechatUser);
}
