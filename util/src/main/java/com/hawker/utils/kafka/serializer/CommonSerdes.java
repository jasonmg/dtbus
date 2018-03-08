package com.hawker.utils.kafka.serializer;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * @author mingjiang.ji on 2017/12/12
 */
public class CommonSerdes<T> implements Serde<T> {

    final private Serializer<T> serializer;
    final private Deserializer<T> deserializer;

    public CommonSerdes() {
        this.serializer = new CommonSerializer<>();
        this.deserializer = new CommonDeserializer<>();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        serializer.configure(configs, isKey);
        deserializer.configure(configs, isKey);
    }

    @Override
    public void close() {
        serializer.close();
        deserializer.close();
    }

    @Override
    public Serializer<T> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<T> deserializer() {
        return deserializer;
    }
}
