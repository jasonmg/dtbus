package com.hawker.utils.kafka.serializer;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author mingjiang.ji on 2017/12/4
 */
public abstract class AvroListDeserializer<T extends SpecificRecordBase> implements Deserializer<List<T>> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    public abstract Schema getSchema();

    @Override
    public List<T> deserialize(String topic, byte[] data) {
        DatumReader<T> userDatumReader = new SpecificDatumReader<>(getSchema());

        BinaryDecoder binaryEncoder = DecoderFactory.get().directBinaryDecoder(new ByteArrayInputStream(data), null);
        List<T> res = new ArrayList<>();

        while (true) {
            try {
                T t = userDatumReader.read(null, binaryEncoder);
                res.add(t);
            } catch (IOException e) {
                break;
            }
        }
        return res;
    }

    @Override
    public void close() {

    }
}
