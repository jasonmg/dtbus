package com.hawker.utils.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.sql.Timestamp;

/**
 * @author mingjiang.ji on 2017/11/16
 */
public class TimestampTypeAdapter extends TypeAdapter<Timestamp> {

    @Override
    public Timestamp read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        String stringValue = reader.nextString();
        try {
            return stringTs(stringValue);
        } catch (IllegalArgumentException e) {
            try {
                return longTs(stringValue);
            } catch (NumberFormatException e1) {
                return null;
            }
        }
    }

    private Timestamp stringTs(String v) {
        return Timestamp.valueOf(v);
    }

    private Timestamp longTs(String v) {
        Long value = Long.valueOf(v);
        return new Timestamp(value);
    }

    @Override
    public void write(JsonWriter writer, Timestamp value) throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }
        writer.value(value.getTime());
    }
}
