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
package org.talend.components.google.drive.data;

import java.util.Date;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.talend.components.google.drive.list.GoogleDriveListDefinition;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

public class GoogleDriveDatastoreDatasetBaseTest {

    protected Schema datasetSchema;

    @Before
    public void setUp() throws Exception {
        datasetSchema = SchemaBuilder.builder().record(GoogleDriveListDefinition.COMPONENT_NAME).fields() //
                .name(GoogleDriveListDefinition.RETURN_ID).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type().nullable()
                .stringType().noDefault()//
                .name(GoogleDriveListDefinition.RETURN_NAME).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type().nullable()
                .stringType().noDefault()//
                .name(GoogleDriveListDefinition.RETURN_MIME_TYPE).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type().nullable()
                .stringType().noDefault()//
                .name(GoogleDriveListDefinition.RETURN_MODIFIED_TIME).prop(SchemaConstants.TALEND_IS_LOCKED, "true")
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'")//
                .prop(SchemaConstants.JAVA_CLASS_FLAG, Date.class.getCanonicalName()) //
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")//
                .type(AvroUtils._logicalTimestamp()).noDefault() //
                .name(GoogleDriveListDefinition.RETURN_SIZE).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type().nullable()
                .longType().noDefault() //
                .name(GoogleDriveListDefinition.RETURN_KIND).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type().nullable()
                .stringType().noDefault() //
                .name(GoogleDriveListDefinition.RETURN_TRASHED).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type().nullable()
                .booleanType().noDefault() //
                // TODO This should be a List<String>
                .name(GoogleDriveListDefinition.RETURN_PARENTS).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type().nullable()
                .stringType().noDefault() //
                .name(GoogleDriveListDefinition.RETURN_WEB_VIEW_LINK).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type()
                .nullable().stringType().noDefault() //
                .endRecord();
        datasetSchema.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
    }
}
