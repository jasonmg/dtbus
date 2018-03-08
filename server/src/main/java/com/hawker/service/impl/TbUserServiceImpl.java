package com.hawker.service.impl;

import com.hawker.dao.TbUserMapper;
import com.hawker.pojo.TbUser;
import com.hawker.service.TbUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by yangliping on 2017/9/28.
 */
@Service
public class TbUserServiceImpl  implements TbUserService{

    @Autowired
    private TbUserMapper tbUserMapper;

    @Override
    public TbUser userLogin(TbUser tbUser) {
        return tbUserMapper.userLogin(tbUser);
    }

    @Override
    public TbUser getUserByUserId(TbUser tbUser) {
        return tbUserMapper.getUserByUserId(tbUser.getUserId());
    }

    @Override
    public int userSave(TbUser tbUser) {
        return tbUserMapper.userSave(tbUser);
    }
}
