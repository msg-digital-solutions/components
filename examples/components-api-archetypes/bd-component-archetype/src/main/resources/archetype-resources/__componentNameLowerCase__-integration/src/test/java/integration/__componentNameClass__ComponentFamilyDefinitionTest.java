// ============================================================================
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
// ============================================================================

package ${package}.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;
import ${packageTalend}.api.ComponentInstaller;
import ${packageTalend}.${componentNameLowerCase}.definition.${componentNameClass}ComponentFamilyDefinition;

public class ${componentNameClass}ComponentFamilyDefinitionTest {

    @Test
    public void testServiceLoader() throws Exception {
        ServiceLoader<ComponentInstaller> spiLoader = ServiceLoader.load(ComponentInstaller.class);
        assertThat(spiLoader, hasItem(isA(${componentNameClass}ComponentFamilyDefinition.class)));
    }
}
