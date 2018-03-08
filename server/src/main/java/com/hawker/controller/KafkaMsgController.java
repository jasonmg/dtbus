package com.hawker.controller;

import com.hawker.pojo.KafkaTopic;
import com.hawker.service.KafkaConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author mingjiang.ji on 2017/9/14
 */
@RestController
@CrossOrigin
@ConfigurationProperties
public class KafkaMsgController {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMsgController.class);

    @Autowired
    private KafkaConsumerService consumerService;

    @Value("${kafka.zookeeper}")
    private String zookeepers;

    private static ConcurrentMap<String, KafkaConsumerService.KafkaHLConsumer> map = new ConcurrentHashMap<>();
    private static ConcurrentMap<String, String> sessionMap = new ConcurrentHashMap<>();

    @MessageMapping(value = "/tail")
    public void tailTopic(KafkaTopic topic, SimpMessageHeaderAccessor headerAccessor) throws UnsupportedEncodingException {
        String t = topic.getTopic();

        String sessionId = headerAccessor.getSessionId();

        if (!map.containsKey(t)) {
            KafkaConsumerService.KafkaHLConsumer consumer =
                    consumerService.new KafkaHLConsumer(zookeepers, "default");
            consumer.run(t, 2);

            map.putIfAbsent(t, consumer);
            sessionMap.putIfAbsent(sessionId, t);
        }
    }

    public static void disconnect(String sessionId) {
        String topic = sessionMap.getOrDefault(sessionId, "");
        KafkaConsumerService.KafkaHLConsumer c = map.get(topic);
        if (c != null) {
            c.shutdown();

            sessionMap.remove(sessionId);
            map.remove(topic);
            logger.info("Topic disconnected: topic: {}, sessionId: {}", topic, sessionId);
        }
    }

}
