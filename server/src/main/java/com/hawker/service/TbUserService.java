package com.hawker.service;

import com.hawker.pojo.TbUser;

/**
 * Created by yangliping on 2017/9/28.
 */
public interface TbUserService {
    /***
     * 验证用户密码
     * @param tbUser
     * @return 返回用户信息
     */
    public TbUser userLogin(TbUser tbUser);

    /***
     * 验证用户id是否存在
     * @param tbUser
     * @return 返回用户信息
     */
    public TbUser getUserByUserId(TbUser tbUser);

    /***
     * 保存用户
     * @param tbUser
     * @return
     */
    public int userSave(TbUser tbUser);

}
