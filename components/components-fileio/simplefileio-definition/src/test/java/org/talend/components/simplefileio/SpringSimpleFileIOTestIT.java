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
package org.talend.components.simplefileio;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.service.spring.SpringTestApp;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestApp.class)
// See https://github.com/spring-projects/spring-boot/issues/33661
@Ignore("Spring Boot Test is based on tomcat, so it needs Jakarta 6.x and we can't import it as test dep because we need the 5.x to compile SimpleFileIOErrorCode.java")
public class SpringSimpleFileIOTestIT extends SimpleFileIOTestITBase {
    // all test case are in the parent class.
}
