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

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class Record2JsonStringConverterTest {

    private Record2JsonStringConverter<SinkRecord> valueSmt = new Record2JsonStringConverter.Value<>();
    private Record2JsonStringConverter<SinkRecord> keySmt = new Record2JsonStringConverter.Key<>();


    @Test
    public void transformRecordValue2JsonStringTest() {
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

        final Struct simpleStruct = new Struct(simpleStructSchema);

        simpleStruct.put("simpleString", "TestString");
        simpleStruct.put("simpleBoolean", true);
        simpleStruct.put("simpleFLOAT32", 1.0f);
        simpleStruct.put("simpleFLOAT64", 2.0d);
        simpleStruct.put("simpleInt8", (byte) 8);
        simpleStruct.put("simpleInt16", (short) 2);
        simpleStruct.put("simpleInt32", 3);
        simpleStruct.put("simpleInt64", 4L);

        final Struct simpleNestedStruct1 = new Struct(nestedSchema);
        simpleNestedStruct1.put("entry", "testEntry");
        final Struct simpleNestedStruct2 = new Struct(nestedSchema);
        simpleNestedStruct2.put("entry", "testEntry2");

        final List<Struct> nestedStructArray = Arrays.asList(simpleNestedStruct1, simpleNestedStruct2);

        simpleStruct.put("nestedArray", nestedStructArray);


        final SinkRecord record = new SinkRecord(null, 0, null, "test", simpleStructSchema, simpleStruct, 0);
        final SinkRecord transformedRecord = valueSmt.apply(record);

        assertEquals(transformedRecord.valueSchema().fields().size(), 1);
        assertEquals(transformedRecord.valueSchema().field("myawesomejsonstringfield").schema(), Schema.STRING_SCHEMA);

        Struct value = (Struct) transformedRecord.value();
        String jsonString = (String) value.get("myawesomejsonstringfield");

        assertEquals(jsonString, "{\"simpleString\": \"TestString\", \"simpleBoolean\": true, \"simpleFLOAT32\": 1.0, \"simpleFLOAT64\": 2.0, \"simpleInt8\": 8, \"simpleInt16\": 2, \"simpleInt32\": 3, \"simpleInt64\": {\"$numberLong\": \"4\"}, \"optionalBoolean\": null, \"optionalString\": null, \"optionalFloat\": null, \"optionalInt\": null, \"nestedArray\": [{\"entry\": \"testEntry\"}, {\"entry\": \"testEntry2\"}]}");
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

        assertEquals(transformedRecord.keySchema().fields().size(), 1);
        assertEquals(transformedRecord.keySchema().field("myawesomejsonstringfield").schema(), Schema.STRING_SCHEMA);

        Struct key = (Struct) transformedRecord.key();
        String jsonString = (String) key.get("myawesomejsonstringfield");

        assertEquals(jsonString,"{\"simpleString\": \"TestString\", \"simpleBoolean\": true}");
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

        assertEquals(transformedRecord.valueSchema().fields().size(), 13);

        assertEquals(transformedRecord.value(),null);

    }

}
