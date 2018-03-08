package com.hawker.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hawker.pojo.Authentication;
import com.hawker.pojo.Result;
import com.hawker.service.AuditService;
import com.hawker.service.ListMysqlService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by 金皓天 on 2017/7/12.
 */

@RestController
@RequestMapping("/dtapi/v1")
//@CrossOrigin
public class ListMysqlController {

    @Autowired
    private ListMysqlService service;

    @Autowired
    private AuditService auditService;

//    @RequestMapping("/listMysql")
//    public Result listMysql(@RequestBody Map<String,String>map){
//        Result result=service.authentication(map.get("username"),map.get("password"),map.get("ip"),map.get("port"));
////        Result result=service.authentication("canal"
////                ,"canal"
////                ,"10.0.31.145"
////                ,"3306");
//        return result;
//    }

    /***
     *  鉴权入库
     * @param auth 鉴权对象
     * @return 鉴权结果
     */
    @RequestMapping(value = "/audits",method = RequestMethod.POST)
    public Result auditSave(@RequestBody Authentication auth){
        Result result=null;
        //根据类型mysql和OS进行不通的鉴权
        if("mysql".equals(auth.getType())){
            result=service.authentication(auth.getAuditName(),auth.getAuditPwd(),
                    auth.getAuditIp(),auth.getAuditPort(),auth.getType(),auth.getUserId());
            }else if("os".equals(auth.getType())){
                auth.setIsVaild(1);
                result=service.authOS(auth);
            }
        return  result;
    }


    /***
     *  查询鉴权信息
     * @return 鉴权信息
     */
    @RequestMapping(value = "/audits",method = RequestMethod.GET)
    public Result auditList(HttpServletRequest request){
//        System.out.println(request.getRequestURL());
//        System.out.println(request.getQueryString());
        //获取前端
        int pagesize=Integer.parseInt(request.getParameter("per_page"));
        int pageno=Integer.parseInt(request.getParameter("page"));

        String auditIp=request.getParameter("auditIp");
        String auditName=request.getParameter("auditName");
        String sorts=request.getParameter("sorts");
        String type=request.getParameter("type");
        String userId=request.getParameter("userId");

        PageHelper.startPage(pageno,pagesize);

        List<Authentication> list =auditService.findAuditByFilter(auditIp,auditName,type,userId,sorts,pageno,pagesize);

        //分页后，实际返回的结果list类型是Page<E>，如果想取出分页信息，需要强制转换为Page<E>，
        Page<Authentication> authenticationPage = (Page<Authentication>)list;
        Result result =  new Result("0","");
        result.setRows((int)authenticationPage.getTotal());
        result.setData(authenticationPage);
        return result;
    }


    /***
     *  更新鉴权,使用update或者PATCH更新
     * @param audit 要更新的对象
     * @return 更新结果
     */
    @RequestMapping(value = "/audits",method = RequestMethod.PATCH)
    public Result auditUpdate(@RequestBody Authentication audit) {
        Result result =  new Result("0","");
        result.setRows(1);
        result.setData(auditService.auditPatch(audit));
        return result;
    }

    /***
     *  删除鉴权,注意这里是逻辑删除
     * @param id 鉴权Id
     * @return 更新结果
     */
    @RequestMapping(value = "/audits",method = RequestMethod.DELETE)
    public Result auditDel(String id) {
        Result result =  new Result("0","");
        result.setRows(1);
        result.setData(auditService.deleteAuditById(id));
        return result;
    }


    //获取库信息
    @RequestMapping("/getDb")
    public Result getDb(Map<String,String>map){
        Result result=service.getDb(map.get("username"),map.get("dbname"));
//
        return result;
    }

    //获取所有权限信息
    @RequestMapping("/getAllAu")
    public Result getAllAu(){
        Result result=service.getAllAu();
        return result;
    }



    @RequestMapping("/getTable")
    public Result getTable(Map<String,String>map){
        Result result=service.getTable(map.get("dbname"));
        return result;
    }


    @RequestMapping("/getField")
    public Result getField(Map<String,String>map){
        Result result=service.getField(map.get("tableName"),map.get("dbname"));
        return result;
    }

}
