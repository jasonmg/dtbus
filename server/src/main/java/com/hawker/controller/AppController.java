package com.hawker.controller;


import com.hawker.dao.TotalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/app")
public class AppController {

    @Autowired
    private TotalMapper ma;

    //简单的后台接口，用于测试
    @RequestMapping("/info")
    public Object info(){
        Map<String,Object> map = new HashMap<>();
        map.put("info","hello hello hello");
//        map.put("c",canal.DynamicDataSource());

        return map;
    }

//
//   @RequestMapping("/mybatiesTest")
//    public Object mybatiesTest(){
////        map.put("c",canal.DynamicDataSource());
//        Channel channel=ma.findById("1");
//        return channel.toString();
//    }
}