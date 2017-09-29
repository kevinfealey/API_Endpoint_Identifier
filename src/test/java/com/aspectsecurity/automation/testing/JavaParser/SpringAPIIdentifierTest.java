package com.aspectsecurity.automation.testing.JavaParser;

import com.aspectsecurity.automation.testing.JavaParser.objects.Endpoint;
import com.aspectsecurity.automation.testing.JavaParser.visitors.SpringAnnotationAnalyzer;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class SpringAPIIdentifierTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private Logger logger;
    private String testFilePath;
    private String packageLessTestFile;

    @Before
    public void setUp() throws Exception {
        logger = LoggerFactory.getLogger(SpringAPIIdentifierTest.class);
        File testResourceDirectory = new File("src/test/resources");
        testFilePath = testResourceDirectory.getAbsolutePath() + "//com//aspectsecurity//automation//testing//JavaParser//test//";
        packageLessTestFile = testResourceDirectory.getAbsolutePath() + "//packageLessEndpoint.java";
        // Reset endpoints to empty between tests
        SpringAPIIdentifier.setEndpoints(new ArrayList<>());
    }

    private CompilationUnit generateCompilationUnitFromFile(String file) throws FileNotFoundException {
        logger.debug("Reading in test file: " + file);

        // Assumes this file is part of this project
        FileInputStream in = new FileInputStream(file);

        logger.debug("Parsing...");

        return JavaParser.parse(in);
    }

    @Test
    public void test_RequestMappingExample() throws FileNotFoundException {

        String testFile = testFilePath + "RequestMappingExample.java";

        CompilationUnit cu = generateCompilationUnitFromFile(testFile);

        logger.debug("Running visitors...");

        // Visit and print the methods names
        cu.accept(new SpringAnnotationAnalyzer(), cu.getPackageDeclaration());

        // Get endpoints we've found
        ArrayList<Endpoint> endpoints = SpringAPIIdentifier.getEndpoints();

        assertEquals(7, endpoints.size());

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // SPRING CONSUMES

        assertEquals(endpoints.get(0).getConsumes().toString(), "[]");
        assertEquals(endpoints.get(1).getConsumes().toString(), "[application/json]");
        assertEquals(endpoints.get(2).getConsumes().toString(), "[MediaType.APPLICATION_JSON_VALUE]");
        // Note: arrays with multiple values have 2 spaces after ","
        assertEquals(endpoints.get(3).getConsumes().toString(),
                "[MediaType.APPLICATION_JSON_VALUE,  MediaType.APPLICATION_XML_VALUE]");
        assertEquals(endpoints.get(4).getConsumes().toString(), "[]");
        assertEquals(endpoints.get(5).getConsumes().toString(), "[]");
        assertEquals(endpoints.get(6).getConsumes().toString(), "[]");

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // SPRING HEADERS

        // The following should throw exceptions

        try {
            fail(endpoints.get(0).getHeaders().get(0).getHttpParameterName());
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Exception thrown, as expected.");
        }

        try {
            fail(endpoints.get(0).getHeaders().get(0).getDefaultValue());
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Exception thrown, as expected.");
        }

        try {
            fail(endpoints.get(1).getHeaders().get(0).getHttpParameterName());
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Exception thrown, as expected.");
        }

        try {
            fail(endpoints.get(1).getHeaders().get(0).getDefaultValue());
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Exception thrown, as expected.");
        }

        try {
            fail(endpoints.get(2).getHeaders().get(0).getHttpParameterName());
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Exception thrown, as expected.");
        }

        try {
            fail(endpoints.get(2).getHeaders().get(0).getDefaultValue());
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Exception thrown, as expected.");
        }

        try {
            fail(endpoints.get(3).getHeaders().get(0).getHttpParameterName());
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Exception thrown, as expected.");
        }

        try {
            fail(endpoints.get(3).getHeaders().get(0).getDefaultValue());
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Exception thrown, as expected.");
        }

        assertEquals("key1", endpoints.get(4).getHeaders().get(0).getHttpParameterName());
        assertEquals("val1", endpoints.get(4).getHeaders().get(0).getDefaultValue());
        assertEquals("key2", endpoints.get(4).getHeaders().get(1).getHttpParameterName());
        assertEquals("val2", endpoints.get(4).getHeaders().get(1).getDefaultValue());
        assertEquals("key1", endpoints.get(5).getHeaders().get(0).getHttpParameterName());
        assertEquals("val1", endpoints.get(5).getHeaders().get(0).getDefaultValue());
        assertEquals("key2", endpoints.get(5).getHeaders().get(1).getHttpParameterName());
        assertEquals("val2", endpoints.get(5).getHeaders().get(1).getDefaultValue());
        assertEquals("key", endpoints.get(6).getHeaders().get(0).getHttpParameterName());
        assertEquals("val", endpoints.get(6).getHeaders().get(0).getDefaultValue());

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // SPRING HTTP METHOD

        assertEquals("[]", endpoints.get(0).getMethods().toString());
        assertEquals("[RequestMethod.GET]", endpoints.get(1).getMethods().toString());
        assertEquals("[RequestMethod.GET]", endpoints.get(2).getMethods().toString());
        assertEquals("[RequestMethod.GET]", endpoints.get(3).getMethods().toString());
        assertEquals("[RequestMethod.POST]", endpoints.get(4).getMethods().toString());
        // Note: arrays with multiple values have 2 spaces after ","
        assertEquals("[RequestMethod.PUT,  RequestMethod.GET]", endpoints.get(5).getMethods().toString());
        assertEquals("[RequestMethod.POST,  RequestMethod.GET,  RequestMethod.PUT]",
                endpoints.get(6).getMethods().toString());

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // SPRING HTTP PRODUCES

        assertEquals(endpoints.get(0).getProduces().toString(), "[]");
        assertEquals(endpoints.get(1).getProduces().toString(), "[]");
        assertEquals(endpoints.get(2).getProduces().toString(), "[]");
        assertEquals(endpoints.get(3).getProduces().toString(), "[]");
        assertEquals(endpoints.get(4).getProduces().toString(), "[]");
        assertEquals(endpoints.get(5).getProduces().toString(), "[application/json,  application/xml]");
        assertEquals(endpoints.get(6).getProduces().toString(), "[application/json]");
    }

    @Test
    public void testSpringEndpointParams() throws FileNotFoundException {

        String testFile = testFilePath + "SpringEndpointParametersExample.java";

        CompilationUnit cu = generateCompilationUnitFromFile(testFile);

        logger.debug("Running visitors...");

        // Visit and print the methods' names
        cu.accept(new SpringAnnotationAnalyzer(), cu.getPackageDeclaration());

        // Get endpoints we've found
        ArrayList<Endpoint> endpoints = SpringAPIIdentifier.getEndpoints();

        //public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) { ...
        assertEquals("name", endpoints.get(0).getParams().get(0).getHttpParameterName());
        assertEquals("name", endpoints.get(0).getParams().get(0).getCodeVariableName());
        assertEquals("String", endpoints.get(0).getParams().get(0).getType());
        assertEquals("World", endpoints.get(0).getParams().get(0).getDefaultValue());
        assertEquals("RequestParam", endpoints.get(0).getParams().get(0).getAnnotation());
        assertFalse(endpoints.get(0).getParams().get(0).isRequired());

        //public String endpoint11( @RequestParam("id9") String id9, @RequestParam("id10") String id10) { ...
        assertEquals("id9", endpoints.get(1).getParams().get(0).getHttpParameterName());
        assertEquals("id9", endpoints.get(1).getParams().get(0).getCodeVariableName());
        assertEquals("String", endpoints.get(1).getParams().get(0).getType());
        assertEquals("", endpoints.get(1).getParams().get(0).getDefaultValue());
        assertEquals("RequestParam", endpoints.get(1).getParams().get(0).getAnnotation());
        assertFalse(endpoints.get(1).getParams().get(0).isRequired());

        //public String endpoint11( @RequestParam("id9") String id9, @RequestParam("id10") String id10) { ...
        assertEquals("id10", endpoints.get(1).getParams().get(1).getHttpParameterName());
        assertEquals("id10", endpoints.get(1).getParams().get(1).getCodeVariableName());
        assertEquals("String", endpoints.get(1).getParams().get(1).getType());
        assertEquals("", endpoints.get(1).getParams().get(1).getDefaultValue());
        assertEquals("RequestParam", endpoints.get(1).getParams().get(1).getAnnotation());
        assertFalse(endpoints.get(1).getParams().get(1).isRequired());

        // The following should throw exceptions - there are no params for this endpoint

        try {
            fail(endpoints.get(2).getParams().get(0).getHttpParameterName());
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Exception thrown, as expected.");
        }

        try {
            fail(endpoints.get(2).getParams().get(0).getCodeVariableName());
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Exception thrown, as expected.");
        }

        try {
            fail(endpoints.get(2).getParams().get(0).getType());
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Exception thrown, as expected.");
        }

        try {
            fail(endpoints.get(2).getParams().get(0).getDefaultValue());
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Exception thrown, as expected.");
        }

        try {
            fail(endpoints.get(2).getParams().get(0).getAnnotation());
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Exception thrown, as expected.");
        }

        //public String endpoint13( @PathVariable("id1") String id1) { ...
        assertEquals("id1", endpoints.get(3).getParams().get(0).getHttpParameterName());
        assertEquals("id1", endpoints.get(3).getParams().get(0).getCodeVariableName());
        assertEquals("String", endpoints.get(3).getParams().get(0).getType());
        assertEquals("", endpoints.get(3).getParams().get(0).getDefaultValue());
        assertEquals("PathVariable", endpoints.get(3).getParams().get(0).getAnnotation());
        assertFalse(endpoints.get(3).getParams().get(0).isRequired());

        //public Greeting endpoint15(@RequestParam(value="name", defaultValue="World", required=true) String name) { ...
        assertEquals("name", endpoints.get(4).getParams().get(0).getHttpParameterName());
        assertEquals("name", endpoints.get(4).getParams().get(0).getCodeVariableName());
        assertEquals("String", endpoints.get(4).getParams().get(0).getType());
        assertEquals("World", endpoints.get(4).getParams().get(0).getDefaultValue());
        assertEquals("RequestParam", endpoints.get(4).getParams().get(0).getAnnotation());
        assertTrue(endpoints.get(4).getParams().get(0).isRequired());
    }

    @Test
    //This test should be enabled once null checks are finished.
    public void testPackagelessEndpoint() throws FileNotFoundException {
        String testFile = packageLessTestFile;

        CompilationUnit cu = generateCompilationUnitFromFile(testFile);

        logger.debug("Running visitors...");

        // Visit and print the methods' names
        cu.accept(new SpringAnnotationAnalyzer(), cu.getPackageDeclaration());

        // Get endpoints we've found
        ArrayList<Endpoint> endpoints = SpringAPIIdentifier.getEndpoints();

        //Just be sure we got something from the file....
        //public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) { ...
        assertEquals("name", endpoints.get(0).getParams().get(0).getHttpParameterName());
    }
}
