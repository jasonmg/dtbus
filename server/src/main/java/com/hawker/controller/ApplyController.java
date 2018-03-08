package com.hawker.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hawker.canal.CanalInstance;
import com.hawker.config.KafkaProducerConfig;
import com.hawker.pojo.*;
import com.hawker.service.ApplyService;
import com.hawker.service.KafkaProducerService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import scala.App;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 金皓天 on 2017/7/17.
 */

@RestController
@RequestMapping("/dtapi/v1")
public class ApplyController {

    @Value("${kafka.broker.address}")
    private String kafkaBrokerAddress;

    @Autowired
    private ApplyService applyService;


    @RequestMapping(value = "/audittree")
    public List<TreeNode> auditTree() {
        return applyService.auditTreeNodes();
    }

    @RequestMapping(value = "/apply",method = RequestMethod.POST)
    public Apply applySave(@RequestBody Apply apply) {
        if(apply.getIsVaild()==null){
            apply.setIsVaild(1);
        }
        //如果全库同步的话，则默认tableid为0
        if(apply.getTableId()==null){
            apply.setTableId(0);
        }

        //获取当前kafka配置
        apply.setKafkaServer(kafkaBrokerAddress);

        //1、插入数据库
        Apply apply1 = applyService.save(apply);
        //2、执行推送到Canal接口
        applyService.applyInstance(apply1);

        return apply1;
    }


    /***
     *  查询鉴权信息
     * @return 鉴权信息
     */
    @RequestMapping(value = "/apply",method = RequestMethod.GET)
    public Result applyList(HttpServletRequest request){
        //获取前端参数
        int pageSize=Integer.parseInt(request.getParameter("per_page"));
        int pageNo=Integer.parseInt(request.getParameter("page"));

        String dbName=request.getParameter("dbName");
        String tableName=request.getParameter("tableName");
        String userId=request.getParameter("userId");
        String sorts=request.getParameter("sorts");

        PageHelper.startPage(pageNo,pageSize);

        List<ApplyVO> list =applyService.findApplyByFilter(null,dbName,tableName,0,userId,sorts);

        //分页后，实际返回的结果list类型是Page<E>，如果想取出分页信息，需要强制转换为Page<E>，
        Page<ApplyVO> applyVOList = (Page<ApplyVO>)list;
        Result result =  new Result("0","");
        result.setRows((int)applyVOList.getTotal());
        result.setData(applyVOList);
        return result;
    }

    /***
     *  同步申请的数据到kafka队列，现在将所有的同步数据发送到同步端
     * @return 成功失败标识
     */
    @RequestMapping(value = "/applysync",method = RequestMethod.GET)
    public Result applySync(String ids) {
        int retcode =applyService.applySync(ids);;

        Result result;
        if(retcode==1){
            result =  new Result("0","");
            result.setRows(1);
        }else{
            result =  new Result("1","");
            result.setRows(0);
        }
        return result;
    }

    /***
     * 根据Id删除申请表，物理删除
     * @param id 申请Id
     * @return
     */
    @RequestMapping(value = "/apply",method = RequestMethod.DELETE)
    public Result applyDel(@RequestParam(value = "id") Integer id) {
        int retcode = applyService.deleteApplyById(id);

        Result result;
        if(retcode==1){
            result =  new Result("0","");
            result.setRows(1);
        }else{
            result =  new Result("1","");
            result.setRows(0);
        }
        return result;
    }
}