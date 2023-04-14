package org.talend.components.service.rest.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;

import java.net.URL;

import org.junit.Test;
import org.talend.components.api.component.runtime.JarRuntimeInfo;

public class ComponentsRegistrySetupTest {

    @Test
    public void testExtractComponentsUrls() {
        // checking with a null string
        ComponentsRegistrySetup registrySetup = new ComponentsRegistrySetup();
        assertThat(registrySetup.extractComponentsUrls(null), arrayWithSize(0));

        // checking with working URLs
        assertThat(registrySetup.extractComponentsUrls("file://foo,file://bar"), arrayWithSize(2));

        // checking with one working URL and one wrong one
        assertThat(registrySetup.extractComponentsUrls("file://foo,groovybaby://bar"), arrayWithSize(1));
    }

    // TODO need more tests on the createDefinitionRegistry

    @Test
    // Add "--add-opens java.base/java.net=ALL-UNNAMED" option to JVM to run this test with java 17
    public void testExtractComponentsUrlsWithMavenProtocol() {
        ComponentsRegistrySetup registrySetup = new ComponentsRegistrySetup();
        new JarRuntimeInfo((URL) null, null, null); // ensure mvn url -

        // checking with working URL
        assertThat(registrySetup.extractComponentsUrls(
                "mvn:org.talend.components/components-jdbc-definition/0.28.18-SNAPSHOT/jar"), arrayWithSize(1));
    }

}
