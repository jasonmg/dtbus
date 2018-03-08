package com.hawker.utils.kafka.serializer;

import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.List;
import java.util.Map;

/**
 * @author mingjiang.ji on 2017/12/4
 */
public abstract class AvroListSerdes<T extends SpecificRecordBase> implements Serde<List<T>> {

    final private Serializer<List<T>> serializer;
    final private Deserializer<List<T>> deserializer;

    public AvroListSerdes() {
        this.serializer = new AvroListSerializer<T>() {
            @Override
            public Schema getSchema() {
                return getAvroSchema();
            }
        };
        this.deserializer = new AvroListDeserializer<T>() {

            @Override
            public Schema getSchema() {
                return getAvroSchema();
            }
        };
    }


    public abstract Schema getAvroSchema();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<List<T>> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<List<T>> deserializer() {
        return deserializer;
    }
}
