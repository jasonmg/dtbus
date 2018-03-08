package com.hawker.service;

import com.hawker.pojo.Authentication;
import com.hawker.pojo.Result;
import org.springframework.stereotype.Service;

/**
 * Created by 金皓天 on 2017/7/13.
 */
@Service
public interface ListMysqlService {

    public Result authentication(String username, String password, String ip, String port, String type,String userId);

    public Result authOS(Authentication authentication);

    public Result getDb(String username,String dbname);

    public Result getAllAu();

    public Result getTable(String dbname);

    public Result getField(String tableName,String dbName);

}
