package com.hawker.utils.kafka.serializer;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author mingjiang.ji on 2017/12/4
 */
public abstract class AvroListSerializer<T extends SpecificRecordBase> implements Serializer<List<T>> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    public abstract Schema getSchema();

    @Override
    public byte[] serialize(String topic, List<T> data) {
        T t = data.get(0);

        DatumWriter<T> userDatumWriter = new SpecificDatumWriter<>(getSchema());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BinaryEncoder binaryEncoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);

        for (T i : data) {
            try {
                userDatumWriter.write(i, binaryEncoder);
            } catch (IOException e) {
                throw new SerializationException(e.getMessage());
            }
        }

        try {
            binaryEncoder.flush();
        } catch (IOException e) {
            throw new SerializationException(e.getMessage());
        }

        return outputStream.toByteArray();

    }

    @Override
    public void close() {

    }
}
