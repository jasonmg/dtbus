package com.hawker.service;

import com.hawker.canal.CanalTable;
import com.hawker.pojo.Apply;
import com.hawker.pojo.ApplyVO;
import com.hawker.pojo.Result;
import com.hawker.pojo.TreeNode;

import java.util.List;
import java.util.Map;

/**
 * Created by 金皓天 on 2017/7/17.
 */
public interface ApplyService {

    /***
     * 获取鉴权组织树
     * @return 树数组
     */
    public List<TreeNode> auditTreeNodes();

    /***
     * 增加申请信息
     * @param apply
     * @return 返回申请对象
     */
    public Apply save(Apply apply);


    /***
     * 分页和条件查询
     * @param auditIp  主机地址
     * @param dbName  数据库名称
     * @param tableName 表名称
     * @param tableId 表id
     * @param sorts   排序
     * @return
     */
    List<ApplyVO> findApplyByFilter(String auditIp, String dbName, String tableName, Integer tableId,String userId, String sorts);


    /***
     * 同步申请的数据到kafka队列
     * @param ids  申请id字符串
     * @return 成功失败标识
     */
    int applySync(String ids);

    /***
     * 新增单个表到canal client接口
     * @param apply  申请对象
     * @return 成功失败标识
     */
    int applyInstance(Apply apply);

    /***
     * 根据ip,库，表，获取表Id,然后更新表到增量模式
     * @param auditIp  ip地址
     * @param dbName    数据库名称
     * @param tableName  表名称
     * @return
     */
    int setToFullPulled(String auditIp, String dbName, String tableName);


    /***
     * canal client拉取所有同步信息
     * @return 所有到列表
     */
    List<CanalTable> applyInstanceList();

    /***
     * 根据id删除申请数据
     * @param id
     * @return
     */
    int deleteApplyById(int id);


    /***
     * 根据鉴权id删除所有申请数据
     * @param auditId 鉴权id
     * @return
     */
    int deleteApplyByAuditId(int auditId);
}
