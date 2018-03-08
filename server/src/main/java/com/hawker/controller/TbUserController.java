package com.hawker.controller;

import com.hawker.pojo.Result;
import com.hawker.pojo.TbUser;
import com.hawker.service.TbUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by yangliping on 2017/9/28.
 */
@RestController
@RequestMapping("/dtapi/v1")
public class TbUserController {

    @Autowired
    private TbUserService tbUserService;

    /***
     *  用户鉴权
     * @return 鉴权信息
     */
    @RequestMapping(value = "/tbUser",method = RequestMethod.POST)
    public Result usrSave(@RequestBody TbUser tbUser) {
        Result result = new Result("0","");
        //获取前端参数
        result.setRows(tbUserService.userSave(tbUser));
        result.setData(tbUser);
        return result;
    }

    /***
     *  用户保存
     * @return 鉴权信息
     */
    @RequestMapping(value = "/tbUserLogin",method = RequestMethod.POST)
    public Result listFiles(@RequestBody TbUser tbUser) {

        Result result = new Result("0","");
        //先判断用户是否存在，如果不存在则返回不存在
        TbUser tbUser1= tbUserService.getUserByUserId(tbUser);
        if(StringUtils.isEmpty(tbUser1)){
            result.setRows(0);
            result.setCode("1");
            result.setErrmsg("用户不存在");
            return result;
        }

        //如果存在，则判断用户密码是否正确
        TbUser tbUser2= tbUserService.userLogin(tbUser);
        if(StringUtils.isEmpty(tbUser2)){
            result.setRows(0);
            result.setCode("2");
            result.setErrmsg("用户密码不正确");
            return  result;
        }
        result.setData(tbUser2);
        result.setRows(1);
        return result;
    }
}
