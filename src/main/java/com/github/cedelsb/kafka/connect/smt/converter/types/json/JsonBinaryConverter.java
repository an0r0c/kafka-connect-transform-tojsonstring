package com.github.cedelsb.kafka.connect.smt.converter.types.json;

import org.bson.BsonBinary;
import org.bson.json.Converter;
import org.bson.json.StrictJsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class JsonBinaryConverter implements Converter<BsonBinary> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonBinaryConverter.class);
    @Override
    public void convert(BsonBinary value, StrictJsonWriter writer) {
        try {
            byte[] b = value.getData();
            String base64 = Base64.getEncoder().encodeToString(b);
            writer.writeStartObject();
            writer.writeString("base64", base64);
            writer.writeEndObject();
        } catch (Exception e) {
            LOGGER.error(String.format("Fail to convert offset %d to JSON binary", value.toString()), e);
        }
    }
}
