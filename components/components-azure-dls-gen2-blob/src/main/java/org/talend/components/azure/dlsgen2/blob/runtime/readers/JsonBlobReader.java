/*
 * Copyright (C) 2006-2020 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.components.azure.dlsgen2.blob.runtime.readers;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonNumber;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetProperties;
import org.talend.daikon.avro.converter.JsonGenericRecordConverter;


public class JsonBlobReader extends BlobReader {

    private static final String NAMESPACE = "org.talend.avro.schema.infer.json";

    public static final String RECORD_NAME = "SchemalessJsonToIndexedRecord";

    private static final Schema NULL = Schema.create(Schema.Type.NULL);

    private static final Schema BOOLEAN = Schema.create(Schema.Type.BOOLEAN);

    private static final Schema DOUBLE = Schema.create(Schema.Type.DOUBLE);

    private static final Schema LONG = Schema.create(Schema.Type.LONG);

    private static final Schema INT = Schema.create(Schema.Type.INT);

    private static final Schema STRING =
            Schema.createUnion(asList(Schema.create(Schema.Type.NULL), Schema.create(Schema.Type.STRING)));

    JsonBlobReader(AzureDlsGen2BlobDatasetProperties configuration, BlobContainerClient service, Iterable<BlobItem> blobItems) {
        super(configuration, service, blobItems);
    }

    @Override
    protected IndexedRecordIterator initRecordIterator(Iterable<BlobItem> blobItems) {
        return new JsonFileRecordIterator(blobItems);
    }

    @Override
    public IndexedRecord readRecord() {
        return super.readRecord();
    }

    private class JsonFileRecordIterator extends IndexedRecordIterator<JsonValue> {

        private InputStream currentItemInputStream;

        private Iterator<JsonValue> jsonRecordIterator;

        private JsonGenericRecordConverter converter;

        private JsonBuilderFactory jsonFactory;

        private JsonReader reader;


        protected JsonFileRecordIterator(Iterable<BlobItem> blobList) {
            super(blobList);
            converter = new JsonGenericRecordConverter(SchemaBuilder.record("json").fields().endRecord());
            peekFirstBlob();
        }

        @Override
        protected void readBlob() {
            closePreviousInputStream();
            try {
                BlobClient blobClient = service.getBlobClient(getCurrentBlob().getName());
                currentItemInputStream = blobClient.getBlockBlobClient().openInputStream();
                reader = Json.createReader((new InputStreamReader(currentItemInputStream, StandardCharsets.UTF_8)));
                JsonStructure structure = reader.read();
                if (structure == null) {
                    jsonRecordIterator = new Iterator<JsonValue>() {

                        @Override
                        public boolean hasNext() {
                            return false;
                        }

                        @Override
                        public JsonValue next() {
                            return null;
                        }
                    };
                } else {
                    if (structure.getValueType() == JsonValue.ValueType.ARRAY) {
                        jsonRecordIterator = structure.asJsonArray().stream().iterator();
                    } else {
                        List<JsonValue> l = new ArrayList<>();
                        l.add(structure.asJsonObject());
                        jsonRecordIterator = l.iterator();
                    }
                }
            } catch (Exception e) {
                LOG.error("[readBlob] {}", e.getMessage());
                throw new ComponentException(e);
            }
        }

        @Override
        protected boolean hasNextBlobRecord() {
            return jsonRecordIterator.hasNext();
        }

        @Override
        protected IndexedRecord convertToRecord(JsonValue next) {
            converter = new JsonGenericRecordConverter(guessSchema(RECORD_NAME, next));
            return converter.convertToAvro(next.asJsonObject().toString());
        }

        @Override
        protected JsonValue peekNextBlobRecord() {
            return jsonRecordIterator.next();
        }

        @Override
        protected void complete() {
            closePreviousInputStream();
        }

        private void closePreviousInputStream() {
            if (currentItemInputStream != null) {
                try {
                    currentItemInputStream.close();
                } catch (IOException e) {
                    LOG.error("Can't close stream: {}.", e.getMessage());
                }
            }
        }

        private Schema guessSchema(final String recordName, final JsonValue element) {
            switch (element.getValueType()) {
            case STRING:
                return STRING;
            case NUMBER:
                final Number number = JsonNumber.class.cast(element).numberValue();
                if (Long.class.isInstance(number)) {
                    return LONG;
                }
                if (Integer.class.isInstance(number)) {
                    return INT;
                }
                return DOUBLE;
            case FALSE:
            case TRUE:
                return BOOLEAN;
            case NULL:
                return NULL;
            case OBJECT:
                final Schema record = Schema.createRecord(recordName, null, NAMESPACE, false);
                record.setFields(element
                                         .asJsonObject()
                                         .entrySet()
                                         .stream()
                                         .map(it -> new Schema.Field(cleanupName(it.getKey()),
                                                                     guessSchema(buildNextName(recordName, it
                                                                             .getKey()), it.getValue()), null, null))
                                         .collect(toList()));
                return record;
            case ARRAY:
                final JsonArray array = element.asJsonArray();
                if (!array.isEmpty()) {
                    return Schema.createArray(guessSchema(buildNextName(recordName, "Array"), array.iterator().next()));
                }
                return Schema.createArray(Schema.create(Schema.Type.NULL));
            default:
                throw new IllegalArgumentException("Unsupported: " + element.toString());
            }
        }

        private String cleanupName(final String name) {
            return name.replaceAll("[^a-zA-Z0-9_]", "");
        }

        private String buildNextName(final String recordName, final String key) {
            if (key.isEmpty()) { // weird but possible
                return recordName + "Empty";
            }
            final String normalized = cleanupName(key);
            return recordName + Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
        }

    }
}
