package com.hawker.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.util.StringUtil;
import com.hawker.pojo.LogApply;
import com.hawker.pojo.LogApplyVO;
import com.hawker.pojo.Node;
import com.hawker.pojo.Result;
import com.hawker.service.LogApplyService;
import com.hawker.service.SSH2Service;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dtapi/v1")
public class LogApplyController {

    @Value("${kafka.broker.address}")
    private String kafkaBrokerAddress;

    @Autowired
    private LogApplyService logApplyService;

    @Autowired
    private SSH2Service ssh2Service;

    @RequestMapping(value = "/logapply",method = RequestMethod.POST)
    @Transactional
    public Result applySave(@RequestBody LogApplyVO logApplyVO) {


        if(logApplyVO.getIsValid()==null){
            logApplyVO.setIsValid(1);
        }

        //获取当前kafka配置
        logApplyVO.setKafkaServer(kafkaBrokerAddress);

        String kakfaTopic=logApplyVO.getAuditIp()+logApplyVO.getFileName().replaceAll("/","_");
        logApplyVO.setKafkaTopic(kakfaTopic);

        //1、构造apply对象，插入数据库
        LogApply logApply = new LogApply();
        logApply.setAuditId(logApplyVO.getAuditId());
        logApply.setFileName(logApplyVO.getFileName());
        logApply.setIsFull(logApplyVO.getIsFull());
        logApply.setRemark(logApplyVO.getRemark());
        logApply.setKafkaTopic(logApplyVO.getKafkaTopic());
        //获取当前kafka配置
        logApply.setKafkaServer(kafkaBrokerAddress);
        LogApply logApply1 = logApplyService.save(logApply);

        //2、部署flume接口
        Result result =  null;
        result = ssh2Service.deployFlume(logApplyVO);
        return result;

    }


    /***
     *  查询鉴权信息
     * @return 鉴权信息
     */
    @RequestMapping(value = "/logapply",method = RequestMethod.GET)
    public Result applyList(HttpServletRequest request){
        //获取前端参数
        int pageSize=Integer.parseInt(request.getParameter("pagesize"));
        int pageNo=Integer.parseInt(request.getParameter("pageno"));

        String auditIp=request.getParameter("auditIp");
        String fileName=request.getParameter("fileName");
        String userId=request.getParameter("userId");
        String sorts=request.getParameter("sorts");

        PageHelper.startPage(pageNo,pageSize);

        List<LogApplyVO> list =logApplyService.findApplyByFilter(auditIp,fileName,userId,sorts);

        //分页后，实际返回的结果list类型是Page<E>，如果想取出分页信息，需要强制转换为Page<E>，
        Page<LogApplyVO> applyVOList = (Page<LogApplyVO>)list;
        Result result =  new Result("0","");
        result.setRows((int)applyVOList.getTotal());
        result.setData(applyVOList);
        return result;
    }

    /***
     *  查询鉴权信息
     * @return 鉴权信息
     */
    @RequestMapping(value = "/listfiles",method = RequestMethod.POST)
    public Result listFiles(@RequestBody LogApplyVO logApplyVO) {

        Result result = new Result("0","");
        //获取前端参数
        String curDir = logApplyVO.getFileName();

        if(StringUtil.isEmpty(curDir)){
            curDir="/";
        }
        List<Node> dirs =ssh2Service.listFiles(logApplyVO,curDir);

        result.setData(dirs);
        return result;
    }

    /***
     * 根据Id删除申请表，物理删除
     * @param id 申请Id
     * @return
     */
    @RequestMapping(value = "/logapply",method = RequestMethod.DELETE)
    public Result applyDel(@RequestParam(value = "id") Integer id) {
        int retcode = logApplyService.deleteLogApplyById(id);

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
