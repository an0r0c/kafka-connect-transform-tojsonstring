package com.github.cedelsb.kafka.connect.smt.converter.types.json;

import org.bson.json.Converter;
import org.bson.json.StrictJsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class JsonDateTimeAsStringConverter implements Converter<Long> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDateTimeAsStringConverter.class);

    private DateTimeFormatter dateTimeFormatter = null;

    private DateTimeFormatter toDateTimeFormat(String dateTimeFormat)
    {
        switch(dateTimeFormat.toUpperCase())
        {
            case "ISO_DATE":
                return DateTimeFormatter.ISO_DATE;
            case "ISO_DATE_TIME":
                return DateTimeFormatter.ISO_DATE_TIME;
            case "ISO_INSTANT":
                return DateTimeFormatter.ISO_INSTANT;
            case "ISO_TIME":
                return DateTimeFormatter.ISO_TIME;
            case "ISO_LOCAL_DATE":
                return DateTimeFormatter.ISO_LOCAL_DATE;
            case "ISO_LOCAL_DATE_TIME":
                return DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            case "ISO_LOCAL_TIME":
                return DateTimeFormatter.ISO_LOCAL_TIME;
            case "RFC_1123_DATE_TIME":
                return DateTimeFormatter.RFC_1123_DATE_TIME;
            case "ISO_ZONED_DATE_TIME":
                return DateTimeFormatter.ISO_ZONED_DATE_TIME;
            case "ISO_OFFSET_DATE":
                return DateTimeFormatter.ISO_OFFSET_DATE;
            case "ISO_OFFSET_DATE_TIME":
                return DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            case "ISO_OFFSET_TIME":
                return DateTimeFormatter.ISO_OFFSET_TIME;
            case "BASIC_ISO_DATE":
                return DateTimeFormatter.BASIC_ISO_DATE;
            case "ISO_ORDINAL_DATE":
                return DateTimeFormatter.ISO_ORDINAL_DATE;
            case "ISO_WEEK_DATE":
                return DateTimeFormatter.ISO_WEEK_DATE;
            default:
                return DateTimeFormatter.ofPattern(dateTimeFormat);
        }
    }

    public JsonDateTimeAsStringConverter(String dateTimeFormat, String zoneId) {
        if (dateTimeFormat == null || dateTimeFormat.isEmpty()) {
            dateTimeFormatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of(zoneId));
        } else {
            dateTimeFormatter = toDateTimeFormat(dateTimeFormat).withZone(ZoneId.of(zoneId));
        }
    }

    @Override
    public void convert(Long value, StrictJsonWriter writer) {
        try {
            Instant instant = new Date(value).toInstant();
            String s = dateTimeFormatter.format(instant);
            writer.writeString(s);
        } catch (Exception e) {
            LOGGER.error(String.format("Fail to convert offset %d to JSON date", value), e);
        }
    }
}
