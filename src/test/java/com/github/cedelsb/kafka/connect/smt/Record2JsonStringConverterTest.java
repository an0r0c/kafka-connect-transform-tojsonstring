/*
 * Copyright © 2021 Christian Edelsbrunner (christian.edelsbrunner@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.cedelsb.kafka.connect.smt;

import org.apache.kafka.connect.data.*;
import org.apache.kafka.connect.data.Date;
import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Record2JsonStringConverterTest {

    private Record2JsonStringConverter<SinkRecord> valueSmt = new Record2JsonStringConverter.Value<>();
    private Record2JsonStringConverter<SinkRecord> keySmt = new Record2JsonStringConverter.Key<>();

    private Schema nestedSchema;
    private Schema simpleStructSchema;
    private Struct simpleStruct;

    @Before
    public void createSchemasAndStructs()
    {

        nestedSchema = SchemaBuilder
                .struct()
                .name("nestedElement")
                .version(1)
                .field("entry", Schema.STRING_SCHEMA)
                .build();

        simpleStructSchema = SchemaBuilder
                .struct()
                .name("testSchema")
                .version(1)
                .field("simpleString", Schema.STRING_SCHEMA)
                .field("simpleBoolean", Schema.BOOLEAN_SCHEMA)
                .field("simpleFLOAT32", Schema.FLOAT32_SCHEMA)
                .field("simpleFLOAT64", Schema.FLOAT64_SCHEMA)
                .field("simpleInt8", Schema.INT8_SCHEMA)
                .field("simpleInt16", Schema.INT16_SCHEMA)
                .field("simpleInt32", Schema.INT32_SCHEMA)
                .field("simpleInt64", Schema.INT64_SCHEMA)
                .field("simpleBytes", Schema.BYTES_SCHEMA)
                .field("optionalBoolean", Schema.OPTIONAL_BOOLEAN_SCHEMA)
                .field("optionalString", Schema.OPTIONAL_STRING_SCHEMA)
                .field("optionalFloat", Schema.OPTIONAL_FLOAT32_SCHEMA)
                .field("optionalInt", Schema.OPTIONAL_INT64_SCHEMA)
                .field("optionalBytes", Schema.OPTIONAL_BYTES_SCHEMA)
                .field("nestedArray", SchemaBuilder.array(nestedSchema))
                .field("simpleDate", Date.SCHEMA)
                .field("simpleTime", Time.SCHEMA)
                .field("simpleTimestamp", Timestamp.SCHEMA)
                .field("simpleDecimal", Decimal.schema(0))
                .build();

        simpleStruct = new Struct(simpleStructSchema);

        simpleStruct.put("simpleString", "TestString");
        simpleStruct.put("simpleBoolean", true);
        simpleStruct.put("simpleFLOAT32", 1.0f);
        simpleStruct.put("simpleFLOAT64", 2.0d);
        simpleStruct.put("simpleInt8", (byte) 8);
        simpleStruct.put("simpleInt16", (short) 2);
        simpleStruct.put("simpleInt32", 3);
        simpleStruct.put("simpleInt64", 4L);
        simpleStruct.put("simpleBytes", new byte[]{75, 97, 102, 107, 97, 32, 114, 111, 99, 107, 115, 33});

        final Struct simpleNestedStruct1 = new Struct(nestedSchema);
        simpleNestedStruct1.put("entry", "testEntry");
        final Struct simpleNestedStruct2 = new Struct(nestedSchema);
        simpleNestedStruct2.put("entry", "testEntry2");

        final List<Struct> nestedStructArray = Arrays.asList(simpleNestedStruct1, simpleNestedStruct2);

        simpleStruct.put("nestedArray", nestedStructArray);

        simpleStruct.put("simpleDate",java.util.Date.from(ZonedDateTime.of(LocalDate.of(2022,12,3), LocalTime.MIDNIGHT, ZoneOffset.UTC).toInstant()));
        simpleStruct.put("simpleTime",java.util.Date.from(ZonedDateTime.of(LocalDate.of(2022,12,3), LocalTime.MIDNIGHT, ZoneOffset.UTC).toInstant()));
        simpleStruct.put("simpleTimestamp",java.util.Date.from(ZonedDateTime.of(LocalDate.of(2022,12,3), LocalTime.NOON, ZoneOffset.UTC).toInstant()));
        simpleStruct.put("simpleDecimal", new BigDecimal("12345.6789"));
    }

    @Test
    public void transformRecordValue2JsonStringTest() {
        final Map<String, Object> props = new HashMap<>();

        props.put("json.string.field.name", "myawesomejsonstringfield");

        valueSmt.configure(props);

        final SinkRecord record = new SinkRecord(null, 0, null, "test", simpleStructSchema, simpleStruct, 0);
        final SinkRecord transformedRecord = valueSmt.apply(record);

        assertEquals(1, transformedRecord.valueSchema().fields().size());
        assertEquals(Schema.STRING_SCHEMA,transformedRecord.valueSchema().field("myawesomejsonstringfield").schema());

        Struct value = (Struct) transformedRecord.value();
        String jsonString = (String) value.get("myawesomejsonstringfield");

        assertEquals("{\"simpleString\": \"TestString\", \"simpleBoolean\": true, \"simpleFLOAT32\": 1.0, \"simpleFLOAT64\": 2.0, \"simpleInt8\": 8, \"simpleInt16\": 2, \"simpleInt32\": 3, \"simpleInt64\": 4, \"simpleBytes\": {\"$binary\": {\"base64\": \"S2Fma2Egcm9ja3Mh\", \"subType\": \"00\"}}, \"optionalBoolean\": null, \"optionalString\": null, \"optionalFloat\": null, \"optionalInt\": null, \"optionalBytes\": null, \"nestedArray\": [{\"entry\": \"testEntry\"}, {\"entry\": \"testEntry2\"}], \"simpleDate\": {\"$date\": \"2022-12-03T00:00:00Z\"}, \"simpleTime\": {\"$date\": \"2022-12-03T00:00:00Z\"}, \"simpleTimestamp\": {\"$date\": \"2022-12-03T12:00:00Z\"}, \"simpleDecimal\": {\"$numberDecimal\": \"12345.6789\"}}",jsonString);
    }

    @Test
    public void transformRecordValue2JsonStringEXTENDEDModeTest() {
        final Map<String, Object> props = new HashMap<>();

        props.put("json.string.field.name", "myawesomejsonstringfield");
        props.put("json.writer.output.mode", "EXTENDED");

        valueSmt.configure(props);

        final SinkRecord record = new SinkRecord(null, 0, null, "test", simpleStructSchema, simpleStruct, 0);
        final SinkRecord transformedRecord = valueSmt.apply(record);

        assertEquals(1,transformedRecord.valueSchema().fields().size());
        assertEquals(Schema.STRING_SCHEMA, transformedRecord.valueSchema().field("myawesomejsonstringfield").schema());

        Struct value = (Struct) transformedRecord.value();
        String jsonString = (String) value.get("myawesomejsonstringfield");

        assertEquals("{\"simpleString\": \"TestString\", \"simpleBoolean\": true, \"simpleFLOAT32\": {\"$numberDouble\": \"1.0\"}, \"simpleFLOAT64\": {\"$numberDouble\": \"2.0\"}, \"simpleInt8\": {\"$numberInt\": \"8\"}, \"simpleInt16\": {\"$numberInt\": \"2\"}, \"simpleInt32\": {\"$numberInt\": \"3\"}, \"simpleInt64\": {\"$numberLong\": \"4\"}, \"simpleBytes\": {\"$binary\": {\"base64\": \"S2Fma2Egcm9ja3Mh\", \"subType\": \"00\"}}, \"optionalBoolean\": null, \"optionalString\": null, \"optionalFloat\": null, \"optionalInt\": null, \"optionalBytes\": null, \"nestedArray\": [{\"entry\": \"testEntry\"}, {\"entry\": \"testEntry2\"}], \"simpleDate\": {\"$date\": {\"$numberLong\": \"1670025600000\"}}, \"simpleTime\": {\"$date\": {\"$numberLong\": \"1670025600000\"}}, \"simpleTimestamp\": {\"$date\": {\"$numberLong\": \"1670068800000\"}}, \"simpleDecimal\": {\"$numberDecimal\": \"12345.6789\"}}",jsonString);
    }

    @Test
    public void transformRecordValue2JsonStringSTRICTModeTest() {
        final Map<String, Object> props = new HashMap<>();

        props.put("json.string.field.name", "myawesomejsonstringfield");
        props.put("json.writer.output.mode", "STRICT");

        valueSmt.configure(props);

        final SinkRecord record = new SinkRecord(null, 0, null, "test", simpleStructSchema, simpleStruct, 0);
        final SinkRecord transformedRecord = valueSmt.apply(record);

        assertEquals(1,transformedRecord.valueSchema().fields().size());
        assertEquals(Schema.STRING_SCHEMA, transformedRecord.valueSchema().field("myawesomejsonstringfield").schema());

        Struct value = (Struct) transformedRecord.value();
        String jsonString = (String) value.get("myawesomejsonstringfield");

        assertEquals("{\"simpleString\": \"TestString\", \"simpleBoolean\": true, \"simpleFLOAT32\": 1.0, \"simpleFLOAT64\": 2.0, \"simpleInt8\": 8, \"simpleInt16\": 2, \"simpleInt32\": 3, \"simpleInt64\": {\"$numberLong\": \"4\"}, \"simpleBytes\": {\"$binary\": \"S2Fma2Egcm9ja3Mh\", \"$type\": \"00\"}, \"optionalBoolean\": null, \"optionalString\": null, \"optionalFloat\": null, \"optionalInt\": null, \"optionalBytes\": null, \"nestedArray\": [{\"entry\": \"testEntry\"}, {\"entry\": \"testEntry2\"}], \"simpleDate\": {\"$date\": 1670025600000}, \"simpleTime\": {\"$date\": 1670025600000}, \"simpleTimestamp\": {\"$date\": 1670068800000}, \"simpleDecimal\": {\"$numberDecimal\": \"12345.6789\"}}",jsonString);
    }

    @Test
    public void transformRecordValue2JsonStringLogicalTypesDatetimeAsStringTest() {
        final Map<String, Object> props = new HashMap<>();

        props.put("json.string.field.name", "myawesomejsonstringfield");
        props.put("json.writer.handle.logical.types", true);
        props.put("json.writer.datetime.logical.types.as", "STRING");

        valueSmt.configure(props);

        final SinkRecord record = new SinkRecord(null, 0, null, "test", simpleStructSchema, simpleStruct, 0);
        final SinkRecord transformedRecord = valueSmt.apply(record);

        assertEquals(1,transformedRecord.valueSchema().fields().size());
        assertEquals(Schema.STRING_SCHEMA, transformedRecord.valueSchema().field("myawesomejsonstringfield").schema());

        Struct value = (Struct) transformedRecord.value();
        String jsonString = (String) value.get("myawesomejsonstringfield");

        assertEquals("{\"simpleString\": \"TestString\", \"simpleBoolean\": true, \"simpleFLOAT32\": 1.0, \"simpleFLOAT64\": 2.0, \"simpleInt8\": 8, \"simpleInt16\": 2, \"simpleInt32\": 3, \"simpleInt64\": 4, \"simpleBytes\": {\"base64\": \"S2Fma2Egcm9ja3Mh\"}, \"optionalBoolean\": null, \"optionalString\": null, \"optionalFloat\": null, \"optionalInt\": null, \"optionalBytes\": null, \"nestedArray\": [{\"entry\": \"testEntry\"}, {\"entry\": \"testEntry2\"}], \"simpleDate\": \"2022-12-03T00:00:00Z\", \"simpleTime\": \"2022-12-03T00:00:00Z\", \"simpleTimestamp\": \"2022-12-03T12:00:00Z\", \"simpleDecimal\": 12345.6789}",jsonString);
    }

    @Test
    public void transformRecordValue2JsonStringLogicalTypesDatetimeAsStringWithZoneIdTest() {
        final Map<String, Object> props = new HashMap<>();

        props.put("json.string.field.name", "myawesomejsonstringfield");
        props.put("json.writer.handle.logical.types", true);
        props.put("json.writer.datetime.logical.types.as", "STRING");
        props.put("json.writer.datetime.zoneid", "Europe/Zurich");

        valueSmt.configure(props);

        final SinkRecord record = new SinkRecord(null, 0, null, "test", simpleStructSchema, simpleStruct, 0);
        final SinkRecord transformedRecord = valueSmt.apply(record);

        assertEquals(1,transformedRecord.valueSchema().fields().size());
        assertEquals(Schema.STRING_SCHEMA, transformedRecord.valueSchema().field("myawesomejsonstringfield").schema());

        Struct value = (Struct) transformedRecord.value();
        String jsonString = (String) value.get("myawesomejsonstringfield");

        assertEquals("{\"simpleString\": \"TestString\", \"simpleBoolean\": true, \"simpleFLOAT32\": 1.0, \"simpleFLOAT64\": 2.0, \"simpleInt8\": 8, \"simpleInt16\": 2, \"simpleInt32\": 3, \"simpleInt64\": 4, \"simpleBytes\": {\"base64\": \"S2Fma2Egcm9ja3Mh\"}, \"optionalBoolean\": null, \"optionalString\": null, \"optionalFloat\": null, \"optionalInt\": null, \"optionalBytes\": null, \"nestedArray\": [{\"entry\": \"testEntry\"}, {\"entry\": \"testEntry2\"}], \"simpleDate\": \"2022-12-03T00:00:00Z\", \"simpleTime\": \"2022-12-03T00:00:00Z\", \"simpleTimestamp\": \"2022-12-03T12:00:00Z\", \"simpleDecimal\": 12345.6789}",jsonString);
    }

    @Test
    public void transformRecordValue2JsonStringLogicalTypesDatetimeAsStringWithDefinedPatternAndZoneIdTest() {
        final Map<String, Object> props = new HashMap<>();

        props.put("json.string.field.name", "myawesomejsonstringfield");
        props.put("json.writer.handle.logical.types", true);
        props.put("json.writer.datetime.logical.types.as", "STRING");
        props.put("json.writer.datetime.pattern", "ISO_DATE_TIME");
        props.put("json.writer.datetime.zoneid", "CET");

        valueSmt.configure(props);

        final SinkRecord record = new SinkRecord(null, 0, null, "test", simpleStructSchema, simpleStruct, 0);
        final SinkRecord transformedRecord = valueSmt.apply(record);

        assertEquals(1,transformedRecord.valueSchema().fields().size());
        assertEquals(Schema.STRING_SCHEMA, transformedRecord.valueSchema().field("myawesomejsonstringfield").schema());

        Struct value = (Struct) transformedRecord.value();
        String jsonString = (String) value.get("myawesomejsonstringfield");

        assertEquals("{\"simpleString\": \"TestString\", \"simpleBoolean\": true, \"simpleFLOAT32\": 1.0, \"simpleFLOAT64\": 2.0, \"simpleInt8\": 8, \"simpleInt16\": 2, \"simpleInt32\": 3, \"simpleInt64\": 4, \"simpleBytes\": {\"base64\": \"S2Fma2Egcm9ja3Mh\"}, \"optionalBoolean\": null, \"optionalString\": null, \"optionalFloat\": null, \"optionalInt\": null, \"optionalBytes\": null, \"nestedArray\": [{\"entry\": \"testEntry\"}, {\"entry\": \"testEntry2\"}], \"simpleDate\": \"2022-12-03T01:00:00+01:00[CET]\", \"simpleTime\": \"2022-12-03T01:00:00+01:00[CET]\", \"simpleTimestamp\": \"2022-12-03T13:00:00+01:00[CET]\", \"simpleDecimal\": 12345.6789}",jsonString);
    }

    @Test
    public void transformRecordValue2JsonStringLogicalTypesDatetimeAsStringWithPatternAndZoneIdTest() {
        final Map<String, Object> props = new HashMap<>();

        props.put("json.string.field.name", "myawesomejsonstringfield");
        props.put("json.writer.handle.logical.types", true);
        props.put("json.writer.datetime.logical.types.as", "STRING");
        props.put("json.writer.datetime.pattern", "dd.MM.yyyy HH:mm z");
        props.put("json.writer.datetime.zoneid", "CET");

        Locale.setDefault(new Locale("en", "GB")); // Force Locale to be English so that the timezone display name is properly returned

        valueSmt.configure(props);

        final SinkRecord record = new SinkRecord(null, 0, null, "test", simpleStructSchema, simpleStruct, 0);
        final SinkRecord transformedRecord = valueSmt.apply(record);

        assertEquals(1,transformedRecord.valueSchema().fields().size());
        assertEquals(Schema.STRING_SCHEMA, transformedRecord.valueSchema().field("myawesomejsonstringfield").schema());

        Struct value = (Struct) transformedRecord.value();
        String jsonString = (String) value.get("myawesomejsonstringfield");

        assertEquals("{\"simpleString\": \"TestString\", \"simpleBoolean\": true, \"simpleFLOAT32\": 1.0, \"simpleFLOAT64\": 2.0, \"simpleInt8\": 8, \"simpleInt16\": 2, \"simpleInt32\": 3, \"simpleInt64\": 4, \"simpleBytes\": {\"base64\": \"S2Fma2Egcm9ja3Mh\"}, \"optionalBoolean\": null, \"optionalString\": null, \"optionalFloat\": null, \"optionalInt\": null, \"optionalBytes\": null, \"nestedArray\": [{\"entry\": \"testEntry\"}, {\"entry\": \"testEntry2\"}], \"simpleDate\": \"03.12.2022 01:00 CET\", \"simpleTime\": \"03.12.2022 01:00 CET\", \"simpleTimestamp\": \"03.12.2022 13:00 CET\", \"simpleDecimal\": 12345.6789}",jsonString);
    }
    @Test
    public void transformRecordValue2JsonStringLogicalTypesDatetimeAsLongTest() {
        final Map<String, Object> props = new HashMap<>();

        props.put("json.string.field.name", "myawesomejsonstringfield");
        props.put("json.writer.handle.logical.types", true);
        props.put("json.writer.datetime.logical.types.as", "LONG");

        valueSmt.configure(props);

        final SinkRecord record = new SinkRecord(null, 0, null, "test", simpleStructSchema, simpleStruct, 0);
        final SinkRecord transformedRecord = valueSmt.apply(record);

        assertEquals(1,transformedRecord.valueSchema().fields().size());
        assertEquals(Schema.STRING_SCHEMA, transformedRecord.valueSchema().field("myawesomejsonstringfield").schema());

        Struct value = (Struct) transformedRecord.value();
        String jsonString = (String) value.get("myawesomejsonstringfield");

        assertEquals("{\"simpleString\": \"TestString\", \"simpleBoolean\": true, \"simpleFLOAT32\": 1.0, \"simpleFLOAT64\": 2.0, \"simpleInt8\": 8, \"simpleInt16\": 2, \"simpleInt32\": 3, \"simpleInt64\": 4, \"simpleBytes\": {\"base64\": \"S2Fma2Egcm9ja3Mh\"}, \"optionalBoolean\": null, \"optionalString\": null, \"optionalFloat\": null, \"optionalInt\": null, \"optionalBytes\": null, \"nestedArray\": [{\"entry\": \"testEntry\"}, {\"entry\": \"testEntry2\"}], \"simpleDate\": 1670025600000, \"simpleTime\": 1670025600000, \"simpleTimestamp\": 1670068800000, \"simpleDecimal\": 12345.6789}",jsonString);
    }

    @Test
    public void transformRecordValue2JsonStringLogicalTypesDatetimeAsWRONGVLAUETest() {
        final Map<String, Object> props = new HashMap<>();

        props.put("json.string.field.name", "myawesomejsonstringfield");
        props.put("json.writer.handle.logical.types", true);
        props.put("json.writer.datetime.logical.types.as", "wrong value");

        try {
            valueSmt.configure(props);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("Wrong value for configuration setting: json.writer.datetime.logical.types.as=wrong value", ex.getMessage());
        }
    }

    @Test
    public void transformRecordKey2JsonStringTest() {
        final Map<String, Object> props = new HashMap<>();

        props.put("json.string.field.name", "myawesomejsonstringfield");

        keySmt.configure(props);

        final Schema simpleStructSchema = SchemaBuilder
                .struct()
                .name("testSchema")
                .version(1)
                .field("simpleString", Schema.STRING_SCHEMA)
                .field("simpleBoolean", Schema.BOOLEAN_SCHEMA)
                .build();

        final Struct simpleStruct = new Struct(simpleStructSchema);

        simpleStruct.put("simpleString", "TestString");
        simpleStruct.put("simpleBoolean", true);

        final SinkRecord record = new SinkRecord(null, 0, simpleStructSchema, simpleStruct, null, "value", 0);
        final SinkRecord transformedRecord = keySmt.apply(record);

        assertEquals(1,transformedRecord.keySchema().fields().size());
        assertEquals(Schema.STRING_SCHEMA, transformedRecord.keySchema().field("myawesomejsonstringfield").schema());

        Struct key = (Struct) transformedRecord.key();
        String jsonString = (String) key.get("myawesomejsonstringfield");

        assertEquals("{\"simpleString\": \"TestString\", \"simpleBoolean\": true}",jsonString);
    }

    @Test
    public void handleTombstoneRecord() {
        final Map<String, Object> props = new HashMap<>();

        props.put("json.string.field.name", "myawesomejsonstringfield");

        valueSmt.configure(props);

        final Schema nestedSchema = SchemaBuilder
                .struct()
                .name("nestedElement")
                .version(1)
                .field("entry", Schema.STRING_SCHEMA)
                .build();

        final Schema simpleStructSchema = SchemaBuilder
                .struct()
                .name("testSchema")
                .version(1)
                .field("simpleString", Schema.STRING_SCHEMA)
                .field("simpleBoolean", Schema.BOOLEAN_SCHEMA)
                .field("simpleFLOAT32", Schema.FLOAT32_SCHEMA)
                .field("simpleFLOAT64", Schema.FLOAT64_SCHEMA)
                .field("simpleInt8", Schema.INT8_SCHEMA)
                .field("simpleInt16", Schema.INT16_SCHEMA)
                .field("simpleInt32", Schema.INT32_SCHEMA)
                .field("simpleInt64", Schema.INT64_SCHEMA)
                .field("optionalBoolean", Schema.OPTIONAL_BOOLEAN_SCHEMA)
                .field("optionalString", Schema.OPTIONAL_STRING_SCHEMA)
                .field("optionalFloat", Schema.OPTIONAL_FLOAT32_SCHEMA)
                .field("optionalInt", Schema.OPTIONAL_INT64_SCHEMA)
                .field("nestedArray", SchemaBuilder.array(nestedSchema))
                .build();

        final SinkRecord record = new SinkRecord(null, 0, null, "test", simpleStructSchema, null, 0);
        final SinkRecord transformedRecord = valueSmt.apply(record);

        assertEquals(13,transformedRecord.valueSchema().fields().size());

        assertEquals(null, transformedRecord.value());

    }

    @Test
    public void transformRecordValue2JsonStringXMLTest() {
        final Map<String, Object> props = new HashMap<>();

        props.put("json.string.field.name", "myawesomejsonstringfield");
        props.put("post.processing.to.xml", true);

        valueSmt.configure(props);

        final SinkRecord record = new SinkRecord(null, 0, null, "test", simpleStructSchema, simpleStruct, 0);
        final SinkRecord transformedRecord = valueSmt.apply(record);

        assertEquals(transformedRecord.valueSchema().fields().size(), 1);
        assertEquals(transformedRecord.valueSchema().field("myawesomejsonstringfield").schema(), Schema.STRING_SCHEMA);

        Struct value = (Struct) transformedRecord.value();
        String jsonString = (String) value.get("myawesomejsonstringfield");

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root>\n" +
                "  <simpleString>TestString</simpleString>\n" +
                "  <simpleBoolean boolean=\"true\">true</simpleBoolean>\n" +
                "  <simpleFLOAT32 number=\"true\">1.0</simpleFLOAT32>\n" +
                "  <simpleFLOAT64 number=\"true\">2.0</simpleFLOAT64>\n" +
                "  <simpleInt8 number=\"true\">8</simpleInt8>\n" +
                "  <simpleInt16 number=\"true\">2</simpleInt16>\n" +
                "  <simpleInt32 number=\"true\">3</simpleInt32>\n" +
                "  <simpleInt64 number=\"true\">4</simpleInt64>\n" +
                "  <simpleBytes>\n" +
                "    <__EQ__binary>\n" +
                "      <base64>S2Fma2Egcm9ja3Mh</base64>\n" +
                "      <subType>00</subType>\n" +
                "    </__EQ__binary>\n" +
                "  </simpleBytes>\n" +
                "  <optionalBoolean null=\"true\"/>\n" +
                "  <optionalString null=\"true\"/>\n" +
                "  <optionalFloat null=\"true\"/>\n" +
                "  <optionalInt null=\"true\"/>\n" +
                "  <optionalBytes null=\"true\"/>\n" +
                "  <nestedArray>\n" +
                "    <entry>testEntry</entry>\n" +
                "  </nestedArray>\n" +
                "  <nestedArray>\n" +
                "    <entry>testEntry2</entry>\n" +
                "  </nestedArray>\n" +
                "  <simpleDate>\n" +
                "    <__EQ__date>2022-12-03T00:00:00Z</__EQ__date>\n" +
                "  </simpleDate>\n" +
                "  <simpleTime>\n" +
                "    <__EQ__date>2022-12-03T00:00:00Z</__EQ__date>\n" +
                "  </simpleTime>\n" +
                "  <simpleTimestamp>\n" +
                "    <__EQ__date>2022-12-03T12:00:00Z</__EQ__date>\n" +
                "  </simpleTimestamp>\n" +
                "  <simpleDecimal>\n" +
                "    <__EQ__numberDecimal>12345.6789</__EQ__numberDecimal>\n" +
                "  </simpleDecimal>\n" +
                "</root>",jsonString);
    }

    @Test
    public void transformRecordValue2JsonStringLogicalTypesTest() {
        final Map<String, Object> props = new HashMap<>();

        props.put("json.string.field.name", "myawesomejsonstringfield");

        valueSmt.configure(props);

        final SinkRecord record = new SinkRecord(null, 0, null, "test", simpleStructSchema, simpleStruct, 0);
        final SinkRecord transformedRecord = valueSmt.apply(record);

        assertEquals(1, transformedRecord.valueSchema().fields().size());
        assertEquals(Schema.STRING_SCHEMA,transformedRecord.valueSchema().field("myawesomejsonstringfield").schema());

        Struct value = (Struct) transformedRecord.value();
        String jsonString = (String) value.get("myawesomejsonstringfield");

        assertEquals("{\"simpleString\": \"TestString\", \"simpleBoolean\": true, \"simpleFLOAT32\": 1.0, \"simpleFLOAT64\": 2.0, \"simpleInt8\": 8, \"simpleInt16\": 2, \"simpleInt32\": 3, \"simpleInt64\": 4, \"simpleBytes\": {\"$binary\": {\"base64\": \"S2Fma2Egcm9ja3Mh\", \"subType\": \"00\"}}, \"optionalBoolean\": null, \"optionalString\": null, \"optionalFloat\": null, \"optionalInt\": null, \"optionalBytes\": null, \"nestedArray\": [{\"entry\": \"testEntry\"}, {\"entry\": \"testEntry2\"}], \"simpleDate\": {\"$date\": \"2022-12-03T00:00:00Z\"}, \"simpleTime\": {\"$date\": \"2022-12-03T00:00:00Z\"}, \"simpleTimestamp\": {\"$date\": \"2022-12-03T12:00:00Z\"}, \"simpleDecimal\": {\"$numberDecimal\": \"12345.6789\"}}",jsonString);
    }
}
