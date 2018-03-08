package com.hawker.controller;

import com.hawker.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 400/500 错误处理
 */
@Controller
@RequestMapping("/error")
public class ExceptionController implements ErrorController {

    private ErrorAttributes errorAttributes;


    /**
     * 初始化ExceptionController
     *
     * @param errorAttributes
     */
    @Autowired
    public ExceptionController(ErrorAttributes errorAttributes) {
        Assert.notNull(errorAttributes, "ErrorAttributes must not be null");
        this.errorAttributes = errorAttributes;
    }

    /**
     * 定义404的JSON数据
     *
     * @return
     */
    @RequestMapping("404")
    @ResponseBody
    public Result error404() {
        return Result.failed(404, "请求地址不存在");
    }


    /**
     * 定义500的错误JSON信息
     *
     * @return
     */
    @RequestMapping(value = "500")
    @ResponseBody
    public Result error500() {
        return Result.failed(500, "系统错误");
    }

    /**
     * 实现错误路径,暂时无用
     *
     * @return
     */
    @Override
    public String getErrorPath() {
        return "";
    }

}
