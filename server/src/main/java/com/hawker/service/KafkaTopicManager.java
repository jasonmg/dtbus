package com.hawker.service;

/**
 * @author mingjiang.ji on 2017/7/24
 */
public class KafkaTopicManager {

    // topic 废弃消息
    public static final String DISCARD_TOPIC_MSG = "DISCARD";

    public static String getTopic(String ip, String port, String db, String table) {
        return String.format("%s_%s_%s_%s", ip, port, db, table);
    }
}
