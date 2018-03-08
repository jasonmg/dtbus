package com.hawker.utils.kafka.serializer;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * @author mingjiang.ji on 2017/11/22
 *
 * https://unmi.cc/apache-avro-serializing-deserializing/
 */
public class Producer<T extends SpecificRecordBase> {

    private KafkaProducer<String, T> producer = null;

    public Producer(Properties p) {
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class.getName());
        producer = new KafkaProducer<>(p);
    }

    public void sendData(String topic, T data) {
        producer.send(new ProducerRecord<>(topic, data),
                (metadata, exception) -> {
                    if (exception == null) {
                        System.out.printf("Sent user: %s \n", data);
                    } else {
                        System.out.println("data sent failed: " + exception.getMessage());
                    }
                });
    }
}
