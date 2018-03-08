package com.hawker.utils.kafka.serializer;

import com.hawker.utils.FileUtil;
import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.hawker.utils.FileUtil.readLines;

/**
 * @author mingjiang.ji on 2017/11/22
 */
public abstract class AvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    public abstract Schema getSchema();

    @Override
    public T deserialize(String topic, byte[] data) {
        DatumReader<T> userDatumReader = new SpecificDatumReader<>(getSchema());

        BinaryDecoder binaryEncoder = DecoderFactory.get().directBinaryDecoder(new ByteArrayInputStream(data), null);
        try {
            return userDatumReader.read(null, binaryEncoder);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void close() {
    }
}
