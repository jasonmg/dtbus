package com.hawker.utils.gson;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * {{
 * Gson gson: Gson = new GsonBuilder()
 * .registerTypeAdapter(classOf[java.lang.Integer], new IntegerTypeAdapter())
 * .registerTypeAdapter(classOf[java.lang.Long], new LongTypeAdapter())
 * .registerTypeAdapter(classOf[Timestamp], new TimestampTypeAdapter())
 * .create()
 * }}
 *
 * @author mingjiang.ji on 2017/11/16
 */
public class IntegerTypeAdapter extends TypeAdapter<Number> {

    @Override
    public void write(JsonWriter jsonWriter, Number number) throws IOException {
        if (number == null) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.value(number);
    }

    @Override
    public Number read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }

        try {
            String value = jsonReader.nextString();
            if ("".equals(value)) {
                return 0;
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new JsonSyntaxException(e);
        }
    }
}
