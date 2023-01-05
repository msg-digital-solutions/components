// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.runtime;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.salesforce.soql.FieldDescription;
import org.talend.components.salesforce.soql.SoqlQuery;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.avro.AvroUtils;

import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;
import org.talend.daikon.avro.SchemaConstants;

public class SalesforceInputReader extends SalesforceReader<IndexedRecord> {

    private final Logger LOG = LoggerFactory.getLogger(SalesforceInputReader.class);

    private transient QueryResult inputResult;

    private transient SObject[] inputRecords;

    private transient int inputRecordsIndex;

    public SalesforceInputReader(RuntimeContainer container, SalesforceSource source, TSalesforceInputProperties props) {
        super(container, source);
        properties = props;
    }

    @Override
    protected Schema getSchema() throws IOException {
        TSalesforceInputProperties inProperties = (TSalesforceInputProperties) properties;
        if (querySchema == null) {
            querySchema = super.getSchema();
            if (inProperties.manualQuery.getValue()) {
                if (AvroUtils.isIncludeAllFields(properties.module.main.schema.getValue())) {
                    final SoqlQuery query = SoqlQuery.getInstance();

                    boolean passSoqlParserValidation = true;
                    try {
                        query.init(inProperties.query.getValue());
                    } catch(Exception e) {
                        passSoqlParserValidation = false;
                        //ignore any parser exception, even no log, as current parser can't support full soql grammar, that log info will mislead user
                    }

                    final List<Schema.Field> copyFieldList = new ArrayList<>();

                    if(passSoqlParserValidation) {
                        fillFieldListByStaticSoqlParser(query, copyFieldList);
                    } else {
                        fillFieldListByResultObject(copyFieldList);
                    }

                    Map<String, Object> objectProps = querySchema.getObjectProps();
                    querySchema = Schema.createRecord(querySchema.getName(), querySchema.getDoc(), querySchema.getNamespace(),
                            querySchema.isError());
                    querySchema.getObjectProps().putAll(objectProps);
                    querySchema.setFields(copyFieldList);
                }
            }
            querySchema.addProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER, inProperties.columnNameDelimiter.getStringValue());
            querySchema.addProp(SalesforceSchemaConstants.VALUE_DELIMITER, inProperties.normalizeDelimiter.getStringValue());
        }
        addDateTimeUTCField(querySchema);
        return querySchema;
    }

    private void fillFieldListByResultObject(List<Field> copyFieldList) {
        SObject currentSObject = getCurrentSObject();
        Iterator<XmlObject> children = currentSObject.getChildren();
        Map<String, XmlObject> columnName2XmlObject = new LinkedHashMap<>();
        int idCount = 0;
        while (children.hasNext()) {
            XmlObject xmlObject = children.next();
            String elementName = xmlObject.getName().getLocalPart();
            if ("Id".equals(elementName) && idCount == 0) {
                // Ignore the first 'Id' field which always return for query.
                idCount++;
                continue;
            }
            if (!columnName2XmlObject.containsKey(elementName)) {
                columnName2XmlObject.put(elementName, xmlObject);
            }
        }

        int typeCount = 0;
        for (Map.Entry<String, XmlObject> columnNameAndXmlObject : columnName2XmlObject.entrySet()) {
            final String columnName = columnNameAndXmlObject.getKey();

            Field se = querySchema.getField(columnName);
            if (se != null) {
                Field field = new Field(se.name(), se.schema(), se.doc(), se.defaultVal());
                Map<String, Object> fieldProps = se.getObjectProps();
                for (String propName : fieldProps.keySet()) {
                    Object propValue = fieldProps.get(propName);
                    if (propValue != null) {
                        field.addProp(propName, propValue);
                    }
                }
                copyFieldList.add(field);
            } else if(isDynamic) {
                //salesforce use name mapping here, if static design schema, is ok, user need to set right name in talend schema
                //but if dynamic schema, column data will be lost, for example:
                //"SELECT COUNT(Id) C1 FROM Account" or "SELECT Id,TotalAmount,convertCurrency(TotalAmount) The_Amount FROM Order"
                //as no C1 and The_Amount in the Account module, so they will be lost, here fix it.

                //here ignore "type", not "Type" which is data column for some module like Account
                if ("type".equals(columnName) && typeCount == 0) {
                    typeCount++;
                    continue;
                }

                final XmlObject xmlObject = columnNameAndXmlObject.getValue();
                final String xmlType = xmlObject.getXmlType()!=null ? xmlObject.getXmlType().getLocalPart() : null;
                final Object value = xmlObject.getValue();
                final Schema fieldSchema;
                if(value!=null) {
                    fieldSchema = inferSchema4FieldFromValue(value, xmlType);
                } else if(xmlType!=null) {
                    fieldSchema = inferSchema4FieldFromXmlType(xmlType);
                } else {
                    fieldSchema = AvroUtils.wrapAsNullable(AvroUtils._string());
                }

                //no length and scale info, and be nullable always as no that info here

                //SalesforceAvroRegistry not make sure columnName is valid as avro field name, here align it
                Field avroField = new Field(columnName, fieldSchema, null, (Object)null);
                copyFieldList.add(avroField);
            }
        }
    }

    private Schema inferSchema4FieldFromValue(final Object value, final String xmlType4DateStyleType) {
        final Schema base;
        if(value instanceof String) {
            base = AvroUtils._string();
        } else if(value instanceof Integer) {
            base = AvroUtils._int();
        } else if(value instanceof Long) {
            base = AvroUtils._long();
        } else if(value instanceof Short) {
            base = AvroUtils._short();
        } else if(value instanceof Byte) {
            base = AvroUtils._byte();
        } else if(value instanceof Boolean) {
            base = AvroUtils._boolean();
        } else if(value instanceof Double) {
            base = AvroUtils._double();
        } else if(value instanceof Float) {
            base = AvroUtils._float();
        } else if(value instanceof BigDecimal) {
            base = AvroUtils._decimal();
        } else if(value instanceof BigInteger) {
            base = AvroUtils._decimal();
        } else if(value instanceof Date) {
            base = AvroUtils._date();
        } else if(value instanceof byte[]) {
            base = AvroUtils._bytes();
        } else {
            base = AvroUtils._string();
        }

        Schema wrappedOne = AvroUtils.wrapAsNullable(base);

        setPattern(xmlType4DateStyleType, wrappedOne);

        //not process AvroUtils._character() as align with SalesforceAvroRegistry.getConverterFromString

        return wrappedOne;
    }

    private Schema inferSchema4FieldFromXmlType(final String xmlType) {
        //see https://developer.salesforce.com/docs/atlas.en-us.200.0.object_reference.meta/object_reference/primitive_data_types.htm
        //and https://www.w3.org/TR/xmlschema-2
        final Schema base;
        switch (xmlType) {
            case "string":
                base = AvroUtils._string();
                break;
            case "int":
                base = AvroUtils._int();
                break;
            case "long":
                base = AvroUtils._long();
                break;
            case "short":
                base = AvroUtils._short();
                break;
            case "byte":
                base = AvroUtils._byte();
                break;
            case "date":
                base = AvroUtils._date();
                break;
            case "time":
                base = AvroUtils._date();
                break;
            case "dateTime":
                base = AvroUtils._date();
                break;
            case "boolean":
                base = AvroUtils._boolean();
                break;
            case "float":
                base = AvroUtils._float();
                break;
            case "double":
                base = AvroUtils._double();
                break;
            case "decimal":
                base = AvroUtils._decimal();
                break;
            case "binary":
            case "base64Binary":
                base = AvroUtils._bytes();
                break;
            default:
                base = AvroUtils._string();
                break;
        }

        final Schema wrappedOne = AvroUtils.wrapAsNullable(base);

        setPattern(xmlType, wrappedOne);

        return wrappedOne;
    }

    private void setPattern(String xmlType, Schema wrappedOne) {
        if(xmlType == null) {
            return;
        }

        switch (xmlType) {
            case "date":
                wrappedOne.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd");
                break;
            case "time":
                wrappedOne.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "HH:mm:ss.SSS'Z'");
                break;
            case "dateTime":
                wrappedOne.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'.000Z'");
                break;
            default:
                break;
        }
    }

    private void fillFieldListByStaticSoqlParser(SoqlQuery query, List<Field> copyFieldList) {
        // logic almost the same as it is in GuessSchema (SalesforceSourseOrSink): TDI-48569
        for (FieldDescription fieldDescription : query.getFieldDescriptions()) {
            final String simpleName = fieldDescription.getSimpleName();
            Field schemaField = querySchema.getField(simpleName);
            final String fullName;
            if (schemaField == null) {
                final Optional<Field> optionalField = querySchema.getFields().stream()
                        .filter(it -> it.name().equalsIgnoreCase(simpleName))
                        .findAny();
                schemaField = optionalField.orElse(null);
                // fix fullName if it doesn't match the real column name
                // case not sensitive
                fullName = optionalField
                        // we already checked that those parts are equal when ignore case
                        // possible problem when we replace not only simple name
                        .map(it -> fieldDescription.getFullName().replace(simpleName, it.name()))
                        // else value doesn't matter, we expect to handle next iteration instead
                        .orElse(null);
            } else {
                fullName = fieldDescription.getFullName();
            }
            if (schemaField == null) {
                continue;
            }

            Field field = new Field(fullName, schemaField.schema(), schemaField.doc(), schemaField.defaultVal());
            Map<String, Object> props = schemaField.getObjectProps();
            for (String propName : props.keySet()) {
                Object propValue = props.get(propName);
                if (propValue != null) {
                    field.addProp(propName, propValue);
                }
            }
            copyFieldList.add(field);
        }
    }

    @Override
    public boolean start() throws IOException {
        try {
            inputResult = executeSalesforceQuery();
            if (inputResult.getSize() == 0) {
                return false;
            }
            inputRecords = inputResult.getRecords();
            inputRecordsIndex = 0;
            boolean start = inputRecords.length > 0;
            if (start) {
                dataCount++;
            }
            return start;
        } catch (ConnectionException e) {
            // Wrap the exception in an IOException.
            throw new IOException(e);
        }
    }

    @Override
    public boolean advance() throws IOException {
        inputRecordsIndex++;

        // Fast return conditions.
        if (inputRecordsIndex < inputRecords.length) {
            dataCount++;
            return true;
        }
        if (inputResult.isDone()) {
            return false;
        }

        try {
            // Get a new result set based on batch size
            inputResult = getConnection().queryMore(inputResult.getQueryLocator());
            inputRecords = inputResult.getRecords();
            inputRecordsIndex = 0;
            boolean advance = inputRecords != null && inputRecords.length > 0;
            if (advance) {
                // New result set available to retrieve
                dataCount++;
            }
            return advance;
        } catch (ConnectionException e) {
            // Wrap the exception in an IOException.
            throw new IOException(e);
        }

    }

    public SObject getCurrentSObject() throws NoSuchElementException {
        return inputRecords[inputRecordsIndex];
    }

    protected QueryResult executeSalesforceQuery() throws IOException, ConnectionException {
        TSalesforceInputProperties inProperties = (TSalesforceInputProperties) properties;
        getConnection().setQueryOptions(inProperties.batchSize.getValue());
        if (inProperties.includeDeleted.getValue()) {
            return getConnection().queryAll(getQueryString(inProperties));
        } else {
            return getConnection().query(getQueryString(inProperties));
        }
    }

    @Override
    public IndexedRecord getCurrent() {
        try {
            return ((SObjectAdapterFactory) getFactory()).convertToAvro(getCurrentSObject());
        } catch (IOException e) {
            throw new ComponentException(e);
        }
    }
}
