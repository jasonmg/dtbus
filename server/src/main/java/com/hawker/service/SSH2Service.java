package com.hawker.service;

/**
 * Created by yangliping on 2017/9/19.
 */
import com.hawker.pojo.Authentication;
import com.hawker.pojo.LogApplyVO;
import com.hawker.pojo.Node;
import com.hawker.pojo.Result;
import com.trilead.ssh2.Connection;

import java.util.List;


public interface SSH2Service {
    public Connection getConn(LogApplyVO logApplyVO);

    /***
     * 验证连接主机接口
     * @param authentication 验证对象
     * @return
     */
    public Result verfyNamePwd(Authentication authentication);

    /***
     * 获取远程目录的子目录和文件
     * @param host
     * @param remoteDir
     * @return
     */
    public List<Node> listFiles(LogApplyVO host, String remoteDir);


    /**
     * 执行shell，返回执行信息
     *
     * @param conn
     * @param shellCmd
     * @return
     */
    public String execShellWithStdout(Connection conn, String shellCmd);

    /***
     *传输本地Jar包到远程目录
     * @param host
     * @param localFile
     * @param remoteFile
     * @return
     */
    public String copyTar(LogApplyVO host, String localFile,String remoteFile,String msg);


    /***
     * 部署flume
     * @param host
     * @return
     */
    Result deployFlume(LogApplyVO host);
}
