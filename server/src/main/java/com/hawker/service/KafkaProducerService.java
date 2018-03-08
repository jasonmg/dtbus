package com.hawker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import static com.hawker.service.KafkaTopicManager.DISCARD_TOPIC_MSG;

/**
 * @author mingjiang.ji on 2017/7/24
 */
@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(final String topic, final String message) {

        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);

        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
                logger.debug("sent topic='{}', message='{}' with offset={}", topic, message.length(), result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(Throwable ex) {
                logger.debug("unable to send topic='{}', message='{}'", topic, message, ex);
            }
        });

    }

    /**
     * 发送废弃topic message，客户端应该监听 DISCARD_TOPIC_MSG，并作相应的处理。
     */
    public void discardTopic(final String topic) {
        send(topic, DISCARD_TOPIC_MSG);
    }
}
