package com.hawker.dao;

import com.hawker.pojo.TbUser;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TbUserMapper {
    //保存用户信息，返回增加的Id
    @Insert("INSERT INTO tb_user(user_id,user_name,user_pwd,is_valid) values (#{userId},#{userName},#{userPwd})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int userSave(TbUser tbUser);


    @Select("SELECT id,user_id,user_name,user_pwd,is_valid,opdate " +
            "FROM tb_user " +
            "where ${strfilter}")
    List<TbUser> selectUserByFilter(@Param("strfilter") String strfilter);


    @Select("SELECT id,user_id,user_name,user_pwd,is_valid,opdate " +
            "FROM tb_user " +
            "where user_id = #{userId} and user_pwd = #{userPwd}")
    TbUser userLogin(TbUser tbUser);


    @Select("SELECT id,user_id,user_name,user_pwd,is_valid,opdate " +
            "FROM tb_user " +
            "where user_id = #{userId}")
    TbUser getUserByUserId(@Param("userId") String userId);
}
