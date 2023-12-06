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
package org.talend.components.azure.dlsgen2.blob;

import java.util.UUID;

import jakarta.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.api.test.AbstractComponentTest2;
import org.talend.components.azure.dlsgen2.blob.dataset.AzureDlsGen2BlobDatasetDefinition;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2BlobDatastoreDefinition;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2ConnectionEditWizardDefinition;
import org.talend.components.azure.dlsgen2.blob.datastore.AzureDlsGen2ConnectionWizardDefinition;
import org.talend.components.azure.dlsgen2.blob.source.AzureDlsGen2BlobInputDefinition;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public class AzureDlsGen2BlobBase extends AbstractComponentTest2 {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Inject
    DefinitionRegistry testComponentRegistry;

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        if (testComponentRegistry == null) {
            testComponentRegistry = new DefinitionRegistry();
            testComponentRegistry.registerComponentFamilyDefinition(new AzureDlsGen2BlobFamilyDefinition());
        }
        return testComponentRegistry;
    }

    private ComponentServiceImpl componentService;

    public ComponentService getComponentService() {
        if (componentService == null) {
            DefinitionRegistry testComponentRegistry = new DefinitionRegistry();
            testComponentRegistry.registerComponentFamilyDefinition(new AzureDlsGen2BlobFamilyDefinition());
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

    @Test
    public void testAllComponentsAreRegistered() {
        assertComponentIsRegistered(DatastoreDefinition.class, AzureDlsGen2BlobDatastoreDefinition.NAME, AzureDlsGen2BlobDatastoreDefinition.class);
        assertComponentIsRegistered(DatasetDefinition.class, AzureDlsGen2BlobDatasetDefinition.NAME, AzureDlsGen2BlobDatasetDefinition.class);
        assertComponentIsRegistered(Definition.class, AzureDlsGen2BlobInputDefinition.NAME, AzureDlsGen2BlobInputDefinition.class);
        assertComponentIsRegistered(AzureDlsGen2ConnectionWizardDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(AzureDlsGen2ConnectionEditWizardDefinition.COMPONENT_WIZARD_NAME);
    }

    public static class RuntimeContainerMock extends DefaultComponentRuntimeContainerImpl {

        @Override
        public String getCurrentComponentId() {
            return "component_" + UUID.randomUUID().toString().replace("-", "").substring(0, 3).toLowerCase();
        }
    }
}