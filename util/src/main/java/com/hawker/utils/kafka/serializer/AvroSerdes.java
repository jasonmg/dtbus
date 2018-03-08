package com.hawker.utils.kafka.serializer;

import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * @author mingjiang.ji on 2017/11/29
 */
public abstract class AvroSerdes<T extends SpecificRecordBase> implements Serde<T> {

    final private Serializer<T> serializer;
    final private Deserializer<T> deserializer;

    public AvroSerdes() {
        this.serializer = new AvroSerializer<T>() {

            @Override
            public Schema getSchema() {
                return getAvroSchema();
            }
        };
        this.deserializer = new AvroDeserializer<T>() {

            @Override
            public Schema getSchema() {
                return getAvroSchema();
            }
        };
    }

    public abstract Schema getAvroSchema();

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
