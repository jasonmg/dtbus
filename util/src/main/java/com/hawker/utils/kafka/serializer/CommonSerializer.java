package com.hawker.utils.kafka.serializer;

import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * @author mingjiang.ji on 2017/12/12
 */
public class CommonSerializer<T> implements Serializer<T> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, T data) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
            oos.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    @Override
    public void close() {

    }
}
