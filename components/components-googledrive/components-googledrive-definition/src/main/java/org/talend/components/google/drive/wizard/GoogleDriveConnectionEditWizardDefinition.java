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
package org.talend.components.google.drive.wizard;

public class GoogleDriveConnectionEditWizardDefinition extends GoogleDriveConnectionWizardDefinition {

    public static final String COMPONENT_WIZARD_NAME = "GoogleDrive.edit";

    @Override
    public String getName() {
        return COMPONENT_WIZARD_NAME;
    }

    @Override
    public boolean isTopLevel() {
        return false;
    }

}
