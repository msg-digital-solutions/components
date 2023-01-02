//============================================================================
//
// Copyright (C) 2006-2023 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
//============================================================================
package org.talend.components.jira.datum;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for retrieving certain values from Entity JSON representation
 */
abstract class EntityParser {

    private static final Logger LOG = LoggerFactory.getLogger(EntityParser.class);

    /**
     * Constant value, which means that positive integer value was not specified
     */
    static final int UNDEFINED = -1;

    /**
     * Constant value, which is with by braceCounter, to specify end of JSON
     */
    private static final int END_JSON = 0;

    /**
     * Parses JSON and searches for total property
     * 
     * @param json JSON string
     * @return total property value, if it is exist or -1 otherwise
     */
    static int getTotal(String json) {
            JsonParser parser = Json.createParser(new ByteArrayInputStream(json.getBytes()));
            JsonObject object = parser.getObject();
            if(object.get("total")==null){
                return UNDEFINED;
            }else {
                return object.getInt("total");
            }
    }

    /**
     * Parses JSON and returns a {@link List} of {@link Entity}
     * 
     * @param json JSON string
     * @param fieldName Name of field, from which retrieve a list of {@link Entity}
     * @return a {@link List} of {@link Entity}
     */
    static List<Entity> getEntities(String json, final String fieldName) {
        JsonParser parser = Json.createParser(new ByteArrayInputStream(json.getBytes()));
        List<Entity> entities = new LinkedList<Entity>();
        JsonArray records;
        if (fieldName != null) {
             records = parser.getObject().getJsonArray(fieldName);
        }else {
            records = parser.getArray();
        }
        records.forEach(
                e->{
                    entities.add(new Entity(e.asJsonObject().toString()));
                }
        );
        return entities;
    }


    /**
     * Entity Parser state
     */
    private enum State {
        INITIAL,
        READ_JSON_OBJECT,
        READ_JSON_ARRAY,
        READ_JSON_STRING
    }

}
