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
package org.talend.components.api.component.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.lang3.StringUtils;
import org.ops4j.pax.url.mvn.ServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsApiErrorCode;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.sandbox.MvnUrlParser;

/**
 * this will locate a read the dependencies file for a given artifact. This file shall be located in :
 * META-INF/mavenGroupId/mavenArtifactId/dependencies.txt this file is generated by the maven-dependency-plugin plugin
 * in a list format.
 * 
 * <pre>
 * {@code

The following files have been resolved:
org.apache.maven:maven-core:jar:3.3.3:compile
org.springframework:spring-beans:jar:4.2.0.RELEASE:test
org.talend.components:components-common:jar:0.4.0.BUILD-SNAPSHOT:compile
log4j:log4j:jar:1.2.17:test
org.eclipse.aether:aether-impl:jar:1.0.0.v20140518:compile
   * }
 * </pre>
 */
public class DependenciesReader {

    private static final Logger LOG = LoggerFactory.getLogger(DependenciesReader.class);

    private String depTxtPath;

    private static ClassLoader classLoader = DependenciesReader.class.getClassLoader();

    /**
     * we use the <code>mavenGroupId<code> and <code>mavenArtifactId<code> to located the file that should be parsed
     */
    public DependenciesReader(String mavenGroupId, String mavenArtifactId) {
        depTxtPath = computeDependenciesFilePath(mavenGroupId, mavenArtifactId);
    }

    /**
     * we use the <code>depTxtPath<code> to located the file that should be parsed
     */
    public DependenciesReader(String depTxtPath) {
        this.depTxtPath = depTxtPath;
    }

    /**
     * this will locate the file META-INF/mavenGroupId/mavenArtifactId/dependencies.txt and parse it to extract the
     * dependencies of the component except the test depenencies.
     * 
     * @param mavenGroupId group id of the component to locate the dep file
     * @param mavenArtifactId artifact id of the component to locate the dep file.
     * @param classLoader used to locate the file using {@link ClassLoader#getResourceAsStream(String)}
     * @return set of string pax-url formated
     * @throws IOException if reading the file failed.
     */
    public Set<String> getDependencies(ClassLoader classLoader) throws IOException {
        ClassLoader zeClassLoader = classLoader;
        if (zeClassLoader == null) {
            zeClassLoader = this.classLoader;
        }
        try (InputStream depStream = zeClassLoader.getResourceAsStream(depTxtPath)) {
            if (depStream == null) {
                throw new ComponentException(ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED,
                        ExceptionContext.withBuilder().put("path", depTxtPath).build());
            } // else we found it so parse it now
            return parseDependencies(depStream);
        }
    }

    /**
     * reads a stream following the maven-dependency-plugin plugin :list format
     * 
     * <pre>
    * {@code
    
    The following files have been resolved:
    org.apache.maven:maven-core:jar:3.3.3:compile
    org.springframework:spring-beans:jar:4.2.0.RELEASE:test
    org.talend.components:components-common:jar:0.4.0.BUILD-SNAPSHOT:compile
    log4j:log4j:jar:1.2.17:test
    org.eclipse.aether:aether-impl:jar:1.0.0.v20140518:compile
       * }
     * </pre>
     * 
     * and return a list of mvn url strings following the
     * <a href="https://ops4j1.jira.com/wiki/display/paxurl/Mvn+Protocol" >pax-urm mvn</a> protocol .<br>
     * this will ignore test and system dependencies.
     * 
     * 
     * 
     * @param depStream of the dependencies file
     * @return a list of maven url strings
     * @throws IOException if read fails.
     */
    public Set<String> parseDependencies(InputStream depStream) throws IOException {
        Set<String> mvnUris = new HashSet<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(depStream, "UTF-8"));
        // java 8 version
        // reader.lines().filter(line -> StringUtils.countMatches(line, ":") > 3).//
        // filter(line -> !line.endsWith("test")).//
        // forEach(line -> mvnUris.add(parseMvnUri(line)));
        while (reader.ready()) {
            String line = reader.readLine();
            if (isRequiredDependency(line)) {
                mvnUris.add(parseMvnUri(line));
            } // else not an expected dependencies so ignore it.
        }
        return mvnUris;
    }

    /**
     * Checks whether dependency is correct and required. Required dependencies are: compile, runtime, provided Also
     * dependency should be fully described. It should contain gav, classifier and scope (totally 5 fields, thus 4 ":"
     * separators between fields)
     * 
     * @param dependencyLine line from dependencies.txt file describing component dependencies
     * @return true, if dependency is required; false - otherwise
     */
    private boolean isRequiredDependency(String dependencyLine) {
        boolean result = (StringUtils.countMatches(dependencyLine, ":") > 3) && !dependencyLine.endsWith("test")
                && !dependencyLine.endsWith("system");
        return result;
    }

    public String getDependencyFilePath() {
        return depTxtPath;
    }

    /**
     * return the location for resolving the depenencies.txt file inside a jar or folder.
     * 
     * @param mavenGroupId, groupId create a location path.
     * @param mavenArtifactId, artifactid used to create the location path.
     * @return "META-INF/maven/" + mavenGroupId + "/" + mavenArtifactId + "/dependencies.txt"
     */
    public static String computeDependenciesFilePath(String mavenGroupId, String mavenArtifactId) {
        return "META-INF/maven/" + mavenGroupId + "/" + mavenArtifactId + "/dependencies.txt";
    }

    /**
     * expecting groupId:artifactId:type[:classifier]:version:scope and output.
     * 
     * <pre>
     * {@code
     * mvn-uri := 'mvn:' [ repository-url '!' ] group-id '/' artifact-id [ '/' [version] [ '/' [type] [ '/' classifier ] ] ] ]
       * }
     * </pre>
     * 
     * @param dependencyString
     * @return pax-url formatted string
     */
    String parseMvnUri(String dependencyString) {
        String trimedDependency = dependencyString.trim();
        String[] splitedDependency = trimedDependency.split(":");
        String groupId = splitedDependency[0];
        String artifactId = splitedDependency[1];
        String type = splitedDependency[2];
        String classifier = null;
        String version = null;
        if (splitedDependency.length > 5) {
            classifier = splitedDependency[3];
            version = splitedDependency[4];
        } else { // else no classifier.
            version = splitedDependency[3];
        }
        // we ignore the scope here
        return "mvn:" + groupId + '/' + artifactId + '/' + version + '/' + type + (classifier != null ? '/' + classifier : "");
    }

    /**
     * Extracts dependencies list of artifact specified by <code>jarUrl</code> It looks for the .jar resource located at
     * <code>jarURL</code> specified with "mvn:" protocol It uses maven groupId and artifactId to find
     * <code>dependencies.txt</code> file inside .jar It reads this file and returns the list of all dependencies.
     * 
     * @param jarMvnUrl {@link URL} to artifact specified with "mvn:protocol" e.g.
     * "mvn:org.talend.components.test/components-test/1.0/jar/jar"
     * @return list of artifact dependencies or empty list in case of error
     */
    public static List<URL> extractDependencies(URL jarMvnUrl) {
        if (jarMvnUrl != null && ServiceConstants.PROTOCOL.equals(jarMvnUrl.getProtocol())) {
            String pathToDepsFile = computeDependenciesFilePath(jarMvnUrl);
            return extractDepenencies(jarMvnUrl, pathToDepsFile);
        } else {
            LOG.error("trying to get depenencies from an non mvn URL :" + jarMvnUrl);
            return Collections.emptyList();
        }
    }

    /**
     * compute the path from the groupId and the artifact id of the jar mvn url.
     * 
     * @return the path
     * @throws IllegalArgumentException if the jarMvnUrl is not of mvn: protocol
     */
    static String computeDependenciesFilePath(URL jarMvnUrl) {
        // make sure that it is a mvn protocol.
        String protocol = jarMvnUrl != null ? jarMvnUrl.getProtocol() : null;
        if (ServiceConstants.PROTOCOL.equals(protocol)) {
            try {
                MvnUrlParser mvnUrlParser = new MvnUrlParser(jarMvnUrl.getPath());
                return computeDependenciesFilePath(mvnUrlParser.getGroup(), mvnUrlParser.getArtifact());
            } catch (MalformedURLException e) {// should never happend cause the paramter is already a URL
                throw TalendRuntimeException.createUnexpectedException(e);
            }
        } else {// unexpected protocol so return null
            throw new IllegalArgumentException("the URL [" + jarMvnUrl + "] should be have the mvn: protocol.");
        }

    }

    /**
     * this will look inside the jar located at jarURL, assuming this is a jar and look for the file at the
     * pathToDepsFile. It will then extract the list of dependencies from that file
     */
    public static List<URL> extractDepenencies(URL jarUrl, String pathToDepsFile) {
        DependenciesReader dependenciesReader = new DependenciesReader(pathToDepsFile);
        try {
            // we assume that the url is a jar/zip file.
            try (JarInputStream jarInputStream = new JarInputStream(jarUrl.openStream())) {
                return DependenciesReader.extractDependencies(dependenciesReader, pathToDepsFile, jarInputStream);
            }
        } catch (IOException e) {
            throw new ComponentException(ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED, e,
                    ExceptionContext.withBuilder().put("path", pathToDepsFile).build());
        }
    }

    /**
     * read the <code>depTxtPath</code> file and extracts the list of URLs from the <code>jarInputStream</code>
     */
    protected static List<URL> extractDependencies(DependenciesReader dependenciesReader, String depTxtPath,
            JarInputStream jarInputStream) throws IOException, MalformedURLException {
        locateDependencyFileEntry(jarInputStream, depTxtPath);
        Set<String> dependencies = dependenciesReader.parseDependencies(jarInputStream);
        // convert the string to URL
        List<URL> result = new ArrayList<>(dependencies.size());
        for (String urlString : dependencies) {
            result.add(new URL(urlString));
        }
        return result;
    }

    /**
     * this will place the jarInputStream at the depTxtPath location or throws a ComponentException if not found
     */
    private static void locateDependencyFileEntry(JarInputStream jarInputStream, String depTxtPath) throws IOException {
        JarEntry nextJarEntry = jarInputStream.getNextJarEntry();
        while (nextJarEntry != null) {
            if (depTxtPath.equals(nextJarEntry.getName())) {// we got it so return it
                return;
            }
            nextJarEntry = jarInputStream.getNextJarEntry();
        }
        throw new ComponentException(ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED,
                ExceptionContext.withBuilder().put("path", depTxtPath).build());
    }
}
