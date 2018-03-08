package com.hawker.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mingjiang.ji on 2017/7/19
 */
@Configuration
@EnableKafka
public class KafkaProducerConfig {

    @Value("${kafka.broker.address}")
    private String kafkaBrokerAddress;

    @Value("${kafka.producer.retries}")
    private String kafkaProducerRetries;

    @Value("${kafka.producer.batch.size}")
    private String kafkaProducerBatchSize;      // 16384

    @Value("${kafka.producer.buffer.memory}")
    private String kafkaProducerBufferMemory;   // 33554432

    @Value("${kafka.producer.linger.ms}")
    private String kafkaProducerLingerMs;       // 2


    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokerAddress);
        props.put(ProducerConfig.RETRIES_CONFIG, kafkaProducerRetries);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaProducerBatchSize);
        props.put(ProducerConfig.LINGER_MS_CONFIG, kafkaProducerLingerMs);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, kafkaProducerBufferMemory);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
