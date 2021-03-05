# kafka-connect-transform-tojsonstring - A simple Record to JSON String SMT
This is a very simple Kafka Connect SMT which takes the entire key or value record and transforms it to a new record which contains exactly one field with a JSON representation of the origin record. 

## Use Cases
The reason why this SMT was built is the known limitation of the JDBC Sink Connector tohandle nested arrays. If you have schema which contains arrays you cannot really use the JDBC Sink Connector because this connector only supports primitive Data Types. 
But sometimes you just need also some arrays from the schema in the RDBMS. If your RDBMS is able to handle JSON Strings this SMT might be the saviour. You can use it to transform the whole record into a single JSON String which can be mapped by the JDBC Sink connector. 
Afterwards you can use the tools offered by the RDBMS to parse and process the JSON String.

But for sure there are also other use cases out there where this SMT might be helpful.

## Restrictions
This SMT was built to transform Records **with a Schema** to a new Record with a Schema but with only one field. 
So this SMT does not work for Schemaless records. 

It also was only tested with Avro Schemas backed by Confluent Schema Registry (but most likely will work for other schema variants too because the deserializer already converted the record to a Connect Record before so it shouldn't be schema specific)

## Configuration
```json5
{
  ////
  "transforms": "tojson",
  "transforms.tojson.type": "com.github.cedelsb.kafka.connect.smt.Record2JsonStringConverter$Value",
  "transforms.tojson.json.string.field.name" : "myawesomejsonstring" // Optional 
  ////
}
```

## Example

### Input 
* Schema (avro syntax)
```json5
{
	"type": "record",
	"name": "MyEntity",
	"fields": [{
		"name": "id",
		"type": "string"
	},
	{
		"name": "name",
		"type": "string"
	},
	{
		"name": "subElements",
		"type": {
			"type": "array",
			"items": {
				"type": "record",
				"name": "element",
				"fields": [{
					"name": "id",
					"type": "string",
					
				}]
			}
		}
	}]
}
```

* Value 

```
-id:myobject
-name:awesomename
-subElements:
  -id:element1
  -id:element2 
```
### Output 
* Schema
```json5
{
	"type": "record",
	"name": "jsonStringSchema",
	"fields": [{
		"name": "jsonstring",
		"type": "string"
	}]
}
```
* Value (of the schema field "jsonstring")
```json5
{
	"id": "record",
	"name": "jsonStringSchema",
	"subElements": [{"id": "element1"},
                    {"id": "element2"}]
	}]
}
```
## Build, installation / deployment
TODO

## Thanks and Acknowledgement
Basic structure of how to build a basic SMT was taken from [kafka-connect-insert-uuid](https://github.com/cjmatta/kafka-connect-insert-uuid)

Logic for transforming a Connect Record into a Json Document is build up on the awesome converter implemented in [kafka-connect-mongodb](https://github.com/hpgrahsl/kafka-connect-mongodb) which safed me a lot of time and nerves :)  

## License Information

This project is licensed according to [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)

```
Copyright (c) 2021. Christian Edelsbrunner (christian.edelsbrunner@gmail.com) 

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
