package com.hawker.service;

import com.hawker.canal.CanalTable;
import com.hawker.pojo.LogApply;
import com.hawker.pojo.LogApplyVO;

import java.util.List;

public interface LogApplyService {
    /***
     * 增加日志文件监控信息
     * @param logApply
     * @return 返回申请对象
     */
    public LogApply save(LogApply logApply);

    /***
     * 根据Ip查询监控到文件
     * @param auditIp 主机Ip
     * @return
     */
    public List<String> getFilesByIp(String auditIp);

    /***
     * 分页和条件查询
     * @param auditIp  主机地址
     * @param fileName 文件名称
     * @param sorts   排序
     * @return
     */
    List<LogApplyVO> findApplyByFilter(String auditIp, String fileName,String userId, String sorts);

    /***
     * 根据id删除申请数据
     * @param id
     * @return
     */
    int deleteLogApplyById(int id);


}
