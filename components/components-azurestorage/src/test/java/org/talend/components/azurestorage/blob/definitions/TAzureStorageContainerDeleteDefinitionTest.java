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
package org.talend.components.azurestorage.blob.definitions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.EnumSet;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.azurestorage.blob.tazurestoragecontainerdelete.TAzureStorageContainerDeleteDefinition;
import org.talend.components.azurestorage.blob.tazurestoragecontainerdelete.TAzureStorageContainerDeleteProperties;

public class TAzureStorageContainerDeleteDefinitionTest extends AzureStorageContainerDefinitionTest {

    @Override
    public void setUp() throws Exception {
        azureStorageContainerDefinition = new TAzureStorageContainerDeleteDefinition();
    }

    @Override
    public void testGetRuntimeInfo() {
        assertNotNull(azureStorageContainerDefinition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.NONE));
    }

    @Override
    public void testGetPropertiesClass() {
        assertEquals(TAzureStorageContainerDeleteProperties.class, azureStorageContainerDefinition.getPropertiesClass());
    }

    @Override
    public void testGetSupportedConnectorTopologies() {
        assertEquals(EnumSet.of(ConnectorTopology.NONE), azureStorageContainerDefinition.getSupportedConnectorTopologies());
    }

    @Override
    public void testGetReturnProperties() {
        assertNotNull(azureStorageContainerDefinition.getReturnProperties());
        assertEquals(2, azureStorageContainerDefinition.getReturnProperties().length);
    }

}
