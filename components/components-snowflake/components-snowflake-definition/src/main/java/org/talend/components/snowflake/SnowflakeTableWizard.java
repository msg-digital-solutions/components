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
package org.talend.components.snowflake;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.daikon.properties.presentation.Form;

/**
 * A single-page wizard that just handles the selection of tables. This must always be created with the connection
 * properties.
 */
public class SnowflakeTableWizard extends ComponentWizard {

    SnowflakeTableListProperties tProps;

    SnowflakeTableWizard(ComponentWizardDefinition def, String repositoryLocation) {
        super(def, repositoryLocation);

        tProps = new SnowflakeTableListProperties("tProps").setRepositoryLocation(getRepositoryLocation());
        tProps.init();
        addForm(tProps.getForm(Form.MAIN));
    }

    public boolean supportsProperties(ComponentProperties properties) {
        return properties instanceof SnowflakeConnectionProperties;
    }

    public void setupProperties(SnowflakeConnectionProperties cProps) {
        tProps.setConnection(cProps);
    }

}
