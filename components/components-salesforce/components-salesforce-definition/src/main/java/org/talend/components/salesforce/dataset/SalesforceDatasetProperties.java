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
package org.talend.components.salesforce.dataset;

import static org.talend.components.salesforce.SalesforceDefinition.DATAPREP_SOURCE_CLASS;
import static org.talend.components.salesforce.SalesforceDefinition.getSandboxedInstance;
import static org.talend.daikon.properties.property.PropertyFactory.newStringList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.salesforce.common.SalesforceErrorCodes;
import org.talend.components.salesforce.common.SalesforceRuntimeSourceOrSink;
import org.talend.components.salesforce.dataprep.SalesforceInputProperties;
import org.talend.components.salesforce.datastore.SalesforceDatastoreDefinition;
import org.talend.components.salesforce.datastore.SalesforceDatastoreProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.properties.property.StringProperty;
import org.talend.daikon.sandbox.SandboxedInstance;

public class SalesforceDatasetProperties extends PropertiesImpl
        implements DatasetProperties<SalesforceDatastoreProperties> {

    /**
     * 
     */
    private static final long serialVersionUID = -8035880860245867110L;

    private final Logger LOGGER = LoggerFactory.getLogger(SalesforceDatasetProperties.class);

    public ReferenceProperties<SalesforceDatastoreProperties> datastore =
            new ReferenceProperties<>("datastore", SalesforceDatastoreDefinition.NAME);

    public Property<SourceType> sourceType = PropertyFactory.newEnum("sourceType", SourceType.class).setRequired();

    public StringProperty moduleName = PropertyFactory.newString("moduleName");

    public Property<List<String>> selectColumnIds = newStringList("selectColumnIds");

    // condition using after SOQL Where statement
    public Property<String> condition = PropertyFactory.newString("condition");

    public Property<String> query = PropertyFactory.newString("query");

    public SchemaProperties main = new SchemaProperties("main");

    public SalesforceDatasetProperties(String name) {
        super(name);
    }

    private void retrieveModules() throws IOException {
        // refresh the module list
        if (sourceType.getValue() == SourceType.MODULE_SELECTION) {
            Consumer consumer = new Consumer() {

                @Override
                public void accept(SalesforceRuntimeSourceOrSink runtime) throws IOException {
                    List<NamedThing> moduleNames = runtime.getSchemaNames(null);
                    moduleName.setPossibleNamedThingValues(filter(moduleNames));
                }
            };
            runtimeTask(consumer);
        }
    }

    private void retrieveModuleFields() throws IOException {
        // refresh the module list
        if (sourceType.getValue() == SourceType.MODULE_SELECTION && StringUtils.isNotEmpty(moduleName.getValue())) {
            Consumer consumer = new Consumer() {

                @Override
                public void accept(SalesforceRuntimeSourceOrSink runtime) throws IOException {
                    List<NamedThing> moduleNames = runtime.getSchemaNames(null);
                    moduleName.setPossibleNamedThingValues(filter(moduleNames));

                    Schema schema = runtime.getEndpointSchema(null, moduleName.getValue());
                    List<NamedThing> columns = new ArrayList<>();
                    for (Schema.Field field : schema.getFields()) {
                        columns.add(new SimpleNamedThing(field.name(), field.name()));
                    }
                    selectColumnIds.setPossibleValues(columns);
                }
            };
            runtimeTask(consumer);
        }
    }

    static Set<String> MODULE_LIST_WHICH_NOT_SUPPORT_BULK_API =
            new HashSet<String>(Arrays.asList("AcceptedEventRelation", "ActivityHistory", "AggregateResult",
                    "AttachedContentDocument", "CaseStatus", "CombinedAttachment", "ContractStatus",
                    "DeclinedEventRelation", "EmailStatus", "LookedUpFromActivity", "Name", "NoteAndAttachment",
                    "OpenActivity", "OwnedContentDocument", "PartnerRole", "ProcessInstanceHistory", "RecentlyViewed",
                    "SolutionStatus", "TaskPriority", "TaskStatus", "UndecidedEventRelation", "UserRecordAccess"));

    private List<NamedThing> filter(List<NamedThing> moduleNames) {
        List<NamedThing> result = new ArrayList<NamedThing>();
        if (moduleNames != null) {
            for (int i = 0; i < moduleNames.size(); i++) {
                NamedThing module = moduleNames.get(i);
                if (!MODULE_LIST_WHICH_NOT_SUPPORT_BULK_API.contains(module.getName())) {
                    result.add(module);
                }
            }
        }
        return result;
    }

    private interface Consumer {

        void accept(SalesforceRuntimeSourceOrSink runtime) throws IOException;
    }

    private void runtimeTask(Consumer task) throws IOException {
        try (SandboxedInstance sandboxedInstance = getSandboxedInstance(DATAPREP_SOURCE_CLASS)) {
            SalesforceRuntimeSourceOrSink runtime = (SalesforceRuntimeSourceOrSink) sandboxedInstance.getInstance();

            SalesforceInputProperties properties = new SalesforceInputProperties("model");
            properties.setDatasetProperties(this);

            throwExceptionIfValidationResultIsError(runtime.initialize(null, properties));
            throwExceptionIfValidationResultIsError(runtime.validate(null));

            task.accept(runtime);
        }
    }

    private void throwExceptionIfValidationResultIsError(ValidationResult validationResult) {
        if (validationResult == null) {
            return;
        }

        if (validationResult.getStatus() == ValidationResult.Result.ERROR) {
            throw TalendRuntimeException.createUnexpectedException(validationResult.getMessage());
        }
    }

    public void afterSourceType() throws IOException {
        // refresh the module list
        retrieveModules();
        moduleName.setValue(null);
        selectColumnIds.setValue(null);
        query.setValue(null);
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterModuleName() throws IOException {
        // refresh the module list
        retrieveModuleFields();
        selectColumnIds.setValue(null);
        query.setValue(null);
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void setupProperties() {
        sourceType.setValue(SourceType.MODULE_SELECTION);
    }

    @Override
    public void setupLayout() {
        Form mainForm = Form.create(this, Form.MAIN);

        mainForm.addRow(Widget.widget(sourceType).setWidgetType(Widget.RADIO_WIDGET_TYPE));
        mainForm.addRow(Widget.widget(query).setWidgetType(Widget.CODE_WIDGET_TYPE).setConfigurationValue(
                Widget.CODE_SYNTAX_WIDGET_CONF, "sql"));
        mainForm.addRow(Widget.widget(moduleName).setWidgetType(Widget.DATALIST_WIDGET_TYPE));
        mainForm.addRow(Widget.widget(selectColumnIds).setWidgetType(Widget.MULTIPLE_VALUE_SELECTOR_WIDGET_TYPE));
        mainForm.addRow(condition);
    }

    /**
     * the method is called back at many places, even some strange places, so it should work only for basic layout, not
     * some action which need runtime support.
     */
    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        boolean useModule = sourceType.getValue() == SourceType.MODULE_SELECTION;
        form.getWidget(moduleName).setVisible(useModule);
        moduleName.setRequired(useModule);
        boolean moduleNameSelect = StringUtils.isNotEmpty(moduleName.getValue());
        form.getWidget(selectColumnIds).setVisible(useModule && moduleNameSelect);
        selectColumnIds.setRequired(useModule && moduleNameSelect);
        form.getWidget(condition).setVisible(useModule && moduleNameSelect);

        // We can not have a hidden field which is required
        form.getWidget(query).setVisible(!useModule);
        query.setRequired(!useModule);
    }

    @Override
    public SalesforceDatastoreProperties getDatastoreProperties() {
        return datastore.getReference();
    }

    public void afterDatastore() {
        try {
            retrieveModules();
        } catch (IOException e) {
            LOGGER.error("error getting salesforce modules", e);
            throw new TalendRuntimeException(SalesforceErrorCodes.UNABLE_TO_RETRIEVE_MODULES, e);
        }
        if (StringUtils.isNotEmpty(moduleName.getValue())) {
            try {
                retrieveModuleFields();
            } catch (IOException e) {
                LOGGER.error("error getting salesforce modules fields", e);
                throw new TalendRuntimeException(SalesforceErrorCodes.UNABLE_TO_RETRIEVE_MODULE_FIELDS, e);
            }
        } // else no module set so no reason to update fields

    }

    @Override
    public void setDatastoreProperties(SalesforceDatastoreProperties datastoreProperties) {
        datastore.setReference(datastoreProperties);
        afterDatastore();
    }

    public enum SourceType {
        MODULE_SELECTION,
        SOQL_QUERY
    }

}
