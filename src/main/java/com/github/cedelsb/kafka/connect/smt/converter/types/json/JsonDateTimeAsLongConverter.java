package com.github.cedelsb.kafka.connect.smt.converter.types.json;

import org.bson.json.Converter;
import org.bson.json.StrictJsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonDateTimeAsLongConverter implements Converter<Long> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDateTimeAsLongConverter.class);

    @Override
    public void convert(Long value, StrictJsonWriter writer) {
        try {
            writer.writeNumber(Long.toString(value));
        } catch (Exception e) {
            LOGGER.error(String.format("Fail to convert offset %d to JSON date", value), e);
        }
    }
}
