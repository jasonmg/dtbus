package com.hawker.utils.kafka.serializer;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author mingjiang.ji on 2017/11/22
 */
public class Consumer<T extends SpecificRecordBase> {

    private KafkaConsumer<String, T> consumer = null;

    public Consumer(Properties p) {
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, AvroDeserializer.class.getName());

        consumer = new KafkaConsumer<>(p);
    }

    public List<T> receive(String topic) {
//        TopicPartition partition = new TopicPartition(topic.topicName, 0);
        consumer.subscribe(Collections.singletonList(topic));
//        consumer.assign(Collections.singletonList(partition));
        ConsumerRecords<String, T> records = consumer.poll(10);

        return StreamSupport.stream(records.spliterator(), false)
                .map(ConsumerRecord::value).collect(Collectors.toList());
    }
}
