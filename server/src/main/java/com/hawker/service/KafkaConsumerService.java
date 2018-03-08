package com.hawker.service;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author mingjiang.ji on 2017/7/24
 */
@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private CountDownLatch latch;

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setCountDownLatch(CountDownLatch c) {
        this.latch = c;
    }

    @KafkaListener(topics = "${kafka.topic.name}")
    public void receive(String message) {
        logger.info("received message='{}'", message);
        latch.countDown();
    }
    
    private final ExecutorService executor = Executors.newFixedThreadPool(50);

    // ===================== Kafka High Level Consumer =======================================

    public class KafkaHLConsumer {
        private ConsumerConnector consumer;

        public KafkaHLConsumer(String zookeeper, String groupId) {
            consumer = Consumer.createJavaConsumerConnector(createConsumerConfig(zookeeper, groupId));
        }

        public void run(String topic, int numThreads) {
            logger.info("================listening topic: {}", topic);
            Map<String, Integer> topicCountMap = new HashMap<>();
            topicCountMap.put(topic, numThreads);

            Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
            List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

            // now launch all the threads
//            executor = Executors.newFixedThreadPool(numThreads);

            // now create an object to consume the messages
            int threadNumber = 0;
            for (final KafkaStream stream : streams) {
                executor.submit(new ConsumerAction(stream, topic, threadNumber));
                threadNumber++;
            }
        }

        public void shutdown() {
            if (consumer != null) consumer.shutdown();
//            if (executor != null) executor.shutdown();
//            try {
//                if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
//                    logger.error("Timed out waiting for consumer threads to shut down, exiting uncleanly");
//                }
//            } catch (InterruptedException e) {
//                logger.error("Interrupted during shutdown, exiting uncleanly");
//            }
        }
    }


    private ConsumerConfig createConsumerConfig(String zookeeper, String groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", zookeeper);
        props.put("group.id", groupId);
        props.put("zookeeper.session.timeout.ms", "4000");
        props.put("zookeeper.sync.time.ms", "2000");
        props.put("auto.commit.interval.ms", "1000");

        return new ConsumerConfig(props);
    }


    public class ConsumerAction implements Runnable {
        private KafkaStream kStream;
        private int threadNumber;
        private String topicName;

        ConsumerAction(KafkaStream stream, String topic, int threadNum) {
            threadNumber = threadNum;
            kStream = stream;
            topicName = topic;
        }

        public void run() {
            ConsumerIterator<byte[], byte[]> it = kStream.iterator();
            while (it.hasNext()) {
                String msg = new String(it.next().message());
                logger.info("Thread {}: {}", threadNumber, msg);
                sendMsg(topicName, msg);
            }
            logger.info("Shutting down Thread: {}", threadNumber);
        }
    }


    private void sendMsg(String topic, String msg) {
        messagingTemplate.convertAndSend("/topic/getResponse/" + topic, msg);
    }


}
