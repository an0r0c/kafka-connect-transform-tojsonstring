package com.github.cedelsb.kafka.connect.smt.converter.types.json;

import org.bson.json.Converter;
import org.bson.json.StrictJsonWriter;
import org.bson.types.Decimal128;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonDecimalConverter implements Converter<Decimal128> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDecimalConverter.class);
    @Override
    public void convert(Decimal128 value, StrictJsonWriter writer) {
        try {
            writer.writeNumber(value.toString());
        } catch (Exception e) {
            LOGGER.error(String.format("Fail to convert offset %d to JSON decimal", value), e);
        }
    }
}
