// ==============================================================================
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
// ==============================================================================

package org.talend.components.service.rest.impl;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.talend.components.api.component.ConnectorTopology.INCOMING;
import static org.talend.components.api.component.runtime.ExecutionEngine.BEAM;
import static org.talend.components.api.component.runtime.ExecutionEngine.DI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.function.Function;

import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterDataSupplier;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.service.rest.RuntimesController;
import org.talend.components.service.rest.dto.SerPropertiesDto;
import org.talend.components.service.rest.dto.UiSpecsPropertiesDto;
import org.talend.components.service.rest.dto.ValidationResultsDto;
import org.talend.daikon.annotation.ServiceImplementation;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

import com.fasterxml.jackson.databind.ObjectMapper;

@ServiceImplementation
@SuppressWarnings("unchecked")
public class RuntimeControllerImpl implements RuntimesController {

    private static final Logger log = LoggerFactory.getLogger(RuntimeControllerImpl.class);

    @Autowired
    private PropertiesHelpers propertiesHelpers;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public ResponseEntity<ValidationResultsDto> validateDataStoreConnection(UiSpecsPropertiesDto propertiesContainer) {
        DatastoreProperties properties = propertiesHelpers.propertiesFromDto(propertiesContainer);
        return doValidateDatastoreConnection(properties);
    }

    @Override
    public ResponseEntity<ValidationResultsDto> validateDataStoreConnection(SerPropertiesDto propertiesContainer) {
        DatastoreProperties properties = propertiesHelpers.propertiesFromDto(propertiesContainer);
        return doValidateDatastoreConnection(properties);
    }

    private ResponseEntity<ValidationResultsDto> doValidateDatastoreConnection(DatastoreProperties properties) {
        DatastoreDefinition<DatastoreProperties> definition = propertiesHelpers.getFirstDefinitionFromProperties(properties);
        try (SandboxedInstance instance = RuntimeUtil.createRuntimeClass(definition.getRuntimeInfo(properties),
                properties.getClass().getClassLoader())) {
            DatastoreRuntime<DatastoreProperties> datastoreRuntime = (DatastoreRuntime) instance.getInstance();
            datastoreRuntime.initialize(null, properties);
            Iterable<ValidationResult> healthChecks = datastoreRuntime.doHealthChecks(null);

            ValidationResultsDto response = new ValidationResultsDto(
                    healthChecks == null ? emptyList() : newArrayList(healthChecks));
            HttpStatus httpStatus = response.getStatus() == ValidationResult.Result.OK ? HttpStatus.OK : HttpStatus.BAD_REQUEST;

            return new ResponseEntity<>(response, httpStatus);
        }
    }

    @Override
    public String getDatasetSchema(UiSpecsPropertiesDto connectionInfo) throws IOException {
        DatasetProperties<?> properties = propertiesHelpers.propertiesFromDto(connectionInfo);
        DatasetDefinition<DatasetProperties<DatastoreProperties>> definition = propertiesHelpers
                .getFirstDefinitionFromProperties(properties);
        return useDatasetRuntime(definition, properties, runtime -> runtime.getSchema().toString(false));
    }

    @Override
    public String getDatasetSchema(SerPropertiesDto connectionInfo) throws IOException {
        DatasetProperties<?> properties = propertiesHelpers.propertiesFromDto(connectionInfo);
        DatasetDefinition<DatasetProperties<DatastoreProperties>> definition = propertiesHelpers
                .getFirstDefinitionFromProperties(properties);
        return useDatasetRuntime(definition, properties, runtime -> runtime.getSchema().toString(false));
    }

    @Override
    public Void getDatasetData(UiSpecsPropertiesDto connectionInfo, //
            Integer from, //
            Integer limit, //
            OutputStream response) {
        DatasetProperties<?> properties = propertiesHelpers.propertiesFromDto(connectionInfo);
        DatasetDefinition<DatasetProperties<DatastoreProperties>> definition = propertiesHelpers
                .getFirstDefinitionFromProperties(properties);
        return useDatasetRuntime(definition, properties, new DatasetContentWriter(response, limit, true));
    }

    @Override
    public Void getDatasetData(SerPropertiesDto connectionInfo, //
            Integer from, //
            Integer limit, //
            OutputStream response) {
        DatasetProperties<?> properties = propertiesHelpers.propertiesFromDto(connectionInfo);
        DatasetDefinition<DatasetProperties<DatastoreProperties>> definition = propertiesHelpers
                .getFirstDefinitionFromProperties(properties);
        return useDatasetRuntime(definition, properties, new DatasetContentWriter(response, limit, true));
    }

    @Override
    public Void getDatasetDataAsBinary(UiSpecsPropertiesDto connectionInfo, //
            Integer from, //
            Integer limit, //
            OutputStream response) {
        DatasetProperties<?> properties = propertiesHelpers.propertiesFromDto(connectionInfo);
        DatasetDefinition<DatasetProperties<DatastoreProperties>> definition = propertiesHelpers
                .getFirstDefinitionFromProperties(properties);
        return useDatasetRuntime(definition, properties, new DatasetContentWriter(response, limit, false));
    }

    @Override
    public Void getDatasetDataAsBinary(SerPropertiesDto connectionInfo, //
            Integer from, //
            Integer limit, //
            OutputStream response) {
        DatasetProperties<?> properties = propertiesHelpers.propertiesFromDto(connectionInfo);
        DatasetDefinition<DatasetProperties<DatastoreProperties>> definition = propertiesHelpers
                .getFirstDefinitionFromProperties(properties);
        return useDatasetRuntime(definition, properties, new DatasetContentWriter(response, limit, false));
    }

    @Override
    public void writeData(InputStream rawPayload) throws IOException {

        // 1) Read payload (with data as a stream of course)
        DatasetWritePayload payload = DatasetWritePayload.readData(rawPayload, mapper);
        String definitionName = payload.getConfiguration().getDefinitionName();

        // 2) Create properties
        Properties properties = propertiesHelpers.propertiesFromDto(payload.getConfiguration());
        if (properties instanceof ComponentProperties) {
            ComponentProperties componentProperties = (ComponentProperties) properties;

            // 3) Retrieve component definition to be able to create the runtime
            final ComponentDefinition definition = propertiesHelpers.getDefinition(ComponentDefinition.class, definitionName);

            // 4) Get the execution engine
            ExecutionEngine executionEngine;
            if (definition.isSupportingExecutionEngines(DI)) {
                executionEngine = DI;
                // 5) Create the sandbox
                try (SandboxedInstance instance = RuntimeUtil.createRuntimeClass(
                        definition.getRuntimeInfo(executionEngine, componentProperties, INCOMING),
                        definition.getClass().getClassLoader())) {
                    Sink datasetRuntimeInstance = (Sink) instance.getInstance();
                    datasetRuntimeInstance.initialize(null, componentProperties);

                    Iterator<IndexedRecord> data = payload.getData();
                    WriteOperation writeOperation = datasetRuntimeInstance.createWriteOperation();
                    // Supplier return null to signify end of data stream => see WriterDataSupplier.writeData
                    WriterDataSupplier<?, IndexedRecord> stringWriterDataSupplier = new WriterDataSupplier<Object, IndexedRecord>(
                            writeOperation, () -> data.hasNext() ? data.next() : null, null);

                    stringWriterDataSupplier.writeData();
                }
            } else if (definition.isSupportingExecutionEngines(BEAM)) {
                throw new UnsupportedOperationException("Beam runtime is not available for dataset write through HTTP API.");
            } else {
                throw new TalendRuntimeException(CommonErrorCodes.UNREGISTERED_DEFINITION);
            }
        } else if (properties instanceof DatasetProperties) {
            throw new UnsupportedOperationException(
                    "HTTP API is only able to write using component implementations. Not " + properties.getClass());
        }
    }

    private <T> T useDatasetRuntime(final DatasetDefinition<DatasetProperties<DatastoreProperties>> datasetDefinition, //
            DatasetProperties datasetProperties, //
            Function<DatasetRuntime<DatasetProperties<DatastoreProperties>>, T> consumer) {

        try (SandboxedInstance instance = RuntimeUtil.createRuntimeClass(datasetDefinition.getRuntimeInfo(datasetProperties),
                datasetProperties.getClass().getClassLoader())) {
            DatasetRuntime<DatasetProperties<DatastoreProperties>> datasetRuntimeInstance = (DatasetRuntime<DatasetProperties<DatastoreProperties>>) instance
                    .getInstance();

            datasetRuntimeInstance.initialize(null, datasetProperties);

            return consumer.apply(datasetRuntimeInstance);
        }
    }

}
