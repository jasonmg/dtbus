package com.hawker.utils.kafka.serializer;

import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

/**
 * @author mingjiang.ji on 2017/12/12
 */
public class CommonDeserializer<T> implements Deserializer<T> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public T deserialize(String topic, byte[] data) {

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
            return (T) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {

    }
}
