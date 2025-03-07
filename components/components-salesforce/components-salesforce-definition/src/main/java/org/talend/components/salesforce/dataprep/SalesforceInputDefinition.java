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
package org.talend.components.salesforce.dataprep;

import static org.talend.components.salesforce.SalesforceDefinition.DATAPREP_SOURCE_CLASS;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.SupportedProduct;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceDefinition;
import org.talend.components.salesforce.SalesforceFamilyDefinition;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * the salesforce input component which work with data store and data set
 * 
 */
public class SalesforceInputDefinition extends AbstractComponentDefinition {

    public static String NAME = "DataPrep" + SalesforceFamilyDefinition.NAME + "Input";

    // TODO: quick fix to let this component available on datastreams, https://jira.talendforge.org/browse/TFD-3839
    public SalesforceInputDefinition() {
        super(NAME, ExecutionEngine.DI, ExecutionEngine.BEAM);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return SalesforceInputProperties.class;
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(connectorTopology);
        return SalesforceDefinition.getCommonRuntimeInfo(DATAPREP_SOURCE_CLASS);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[0];
    }

    @Override
    public List<String> getSupportedProducts() {
        return Arrays.asList(SupportedProduct.DATAPREP);
    }

    @Override
    public String getIconKey() {
        return "file-salesforce";
    }

    @Override
    public String[] getFamilies() {
        return new String[] { SalesforceFamilyDefinition.NAME };
    }
}
