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
package org.talend.components.api.service.internal.osgi;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.runtime.RuntimeContext;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * This is the OSGI specific service implementation that completely delegates the implementation to the Framework
 * agnostic {@link ComponentServiceImpl}
 */
@Component
public class ComponentServiceOsgi implements ComponentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentServiceOsgi.class);

    GlobalI18N gctx;

    @Reference
    public void osgiInjectGlobalContext(GlobalI18N aGctx) {
        this.gctx = aGctx;
    }

    @Reference
    public void osgiInjectDefinitionRegistry(DefinitionRegistry defReg) {
        this.componentServiceDelegate = new ComponentServiceImpl(defReg);
    }

    private ComponentServiceImpl componentServiceDelegate;

    @Override
    public ComponentProperties getComponentProperties(String name) {
        return componentServiceDelegate.getComponentProperties(name);
    }

    @Override
    public ComponentDefinition getComponentDefinition(String name) {
        return componentServiceDelegate.getComponentDefinition(name);
    }

    @Override
    public ComponentWizard getComponentWizard(String name, String userData) {
        return componentServiceDelegate.getComponentWizard(name, userData);
    }

    @Override
    public List<ComponentWizard> getComponentWizardsForProperties(ComponentProperties properties, String location) {
        return componentServiceDelegate.getComponentWizardsForProperties(properties, location);
    }

    @Override
    public List<ComponentDefinition> getPossibleComponents(ComponentProperties... properties) throws Throwable {
        return componentServiceDelegate.getPossibleComponents(properties);
    }

    @Override
    public Properties makeFormCancelable(Properties properties, String formName) {
        return componentServiceDelegate.makeFormCancelable(properties, formName);
    }

    @Override
    public Properties cancelFormValues(Properties properties, String formName) {
        return componentServiceDelegate.cancelFormValues(properties, formName);
    }

    public ComponentProperties commitFormValues(ComponentProperties properties, String formName) {
        // FIXME - remove this
        return properties;
    }

    @Override
    public Properties validateProperty(String propName, Properties properties) throws Throwable {
        return componentServiceDelegate.validateProperty(propName, properties);
    }

    @Override
    public Properties validateProperty(String propName, Properties properties, RuntimeContext context) throws Throwable {
        return componentServiceDelegate.validateProperty(propName, properties, context);
    }

    @Override
    public Properties beforePropertyActivate(String propName, Properties properties) throws Throwable {
        return componentServiceDelegate.beforePropertyActivate(propName, properties);
    }

    @Override
    public Properties beforePropertyActivate(String propName, Properties properties, RuntimeContext context) throws Throwable {
        return componentServiceDelegate.beforePropertyActivate(propName, properties, context);
    }

    @Override
    public Properties beforePropertyPresent(String propName, Properties properties) throws Throwable {
        return componentServiceDelegate.beforePropertyPresent(propName, properties);
    }

    @Override
    public Properties beforePropertyPresent(String propName, Properties properties, RuntimeContext context) throws Throwable {
        return componentServiceDelegate.beforePropertyPresent(propName, properties, context);
    }

    @Override
    public Properties afterProperty(String propName, Properties properties) throws Throwable {
        return componentServiceDelegate.afterProperty(propName, properties);
    }

    @Override
    public Properties afterProperty(String propName, Properties properties, RuntimeContext context) throws Throwable {
        return componentServiceDelegate.afterProperty(propName, properties, context);
    }

    @Override
    public Properties beforeFormPresent(String formName, Properties properties) throws Throwable {
        return componentServiceDelegate.beforeFormPresent(formName, properties);
    }

    @Override
    public Properties beforeFormPresent(String formName, Properties properties, RuntimeContext context) throws Throwable {
        return componentServiceDelegate.beforeFormPresent(formName, properties, context);
    }

    @Override
    public Properties afterFormNext(String formName, Properties properties) throws Throwable {
        return componentServiceDelegate.afterFormNext(formName, properties);
    }

    @Override
    public Properties afterFormNext(String formName, Properties properties, RuntimeContext context) throws Throwable {
        return componentServiceDelegate.afterFormNext(formName, properties, context);
    }

    @Override
    public Properties afterFormBack(String formName, Properties properties) throws Throwable {
        return componentServiceDelegate.afterFormBack(formName, properties);
    }

    @Override
    public Properties afterFormBack(String formName, Properties properties, RuntimeContext context) throws Throwable {
        return componentServiceDelegate.afterFormBack(formName, properties, context);
    }

    @Override
    public Properties afterFormFinish(String formName, Properties properties) throws Throwable {
        return componentServiceDelegate.afterFormFinish(formName, properties);
    }

    @Override
    public Properties afterFormFinish(String formName, Properties properties, RuntimeContext context) throws Throwable {
        return componentServiceDelegate.afterFormFinish(formName, properties, context);
    }

    @Override
    public Set<String> getAllComponentNames() {
        return componentServiceDelegate.getAllComponentNames();
    }

    @Override
    public Set<ComponentDefinition> getAllComponents() {
        return componentServiceDelegate.getAllComponents();
    }

    @Override
    public Set<ComponentWizardDefinition> getTopLevelComponentWizards() {
        return componentServiceDelegate.getTopLevelComponentWizards();
    }

    @Override
    public InputStream getWizardPngImage(String wizardName, WizardImageType imageType) {
        return componentServiceDelegate.getWizardPngImage(wizardName, imageType);
    }

    @Override
    public InputStream getComponentPngImage(String componentName, ComponentImageType imageType) {
        return componentServiceDelegate.getComponentPngImage(componentName, imageType);
    }

    @Override
    public String storeProperties(Properties properties, String name, String repositoryLocation, String schemaPropertyName) {
        return componentServiceDelegate.storeProperties(properties, name, repositoryLocation, schemaPropertyName);
    }

    @Override
    public void setRepository(Repository repository) {
        componentServiceDelegate.setRepository(repository);
    }

    @Override
    public Schema getSchema(ComponentProperties componentProperties, Connector connector, boolean isOuput) {
        return componentServiceDelegate.getSchema(componentProperties, connector, isOuput);
    }

    @Override
    public Set<? extends Connector> getAvailableConnectors(ComponentProperties componentProperties,
            Set<? extends Connector> connectedConnetor, boolean isOuput) {
        return componentServiceDelegate.getAvailableConnectors(componentProperties, connectedConnetor, isOuput);
    }

    @Override
    public void setSchema(ComponentProperties componentProperties, Connector connector, Schema schema, boolean isOuput) {
        componentServiceDelegate.setSchema(componentProperties, connector, schema, isOuput);
    }

    @Override
    public boolean setNestedPropertiesValues(ComponentProperties targetProperties, Properties nestedValues) {
        return componentServiceDelegate.setNestedPropertiesValues(targetProperties, nestedValues);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(String componentName, ExecutionEngine engine, Properties properties,
            ConnectorTopology componentType) {
        return componentServiceDelegate.getRuntimeInfo(componentName, engine, properties, componentType);
    }

    @Override
    public <T extends Properties> void postDeserialize(T props) {
        componentServiceDelegate.postDeserialize(props);
    }

}
