package com.aspectsecurity.automation.testing.JavaParser;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectsecurity.automation.testing.JavaParser.objects.Endpoint;
import com.aspectsecurity.automation.testing.JavaParser.visitors.SpringAnnotationAnalyzer;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class SpringAPIIdentifierTest {

	Logger logger;
	  @Rule
	  public final ExpectedException exception = ExpectedException.none();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
	}

	@Before
	public void setUp() throws Exception {
		logger = LoggerFactory.getLogger(SpringAPIIdentifierTest.class);
		//reset Endpoints to empty between tests
		SpringAPIIdentifier.setEndpoints(new ArrayList<Endpoint>());
	}
	
	private CompilationUnit generateCompilationUnitFromFile(String file) throws FileNotFoundException{
		logger.debug("Reading in test file: " + file);
		// Assumes this file is part of this project
		FileInputStream in = new FileInputStream(file);

		logger.debug("Parsing...");
		// parse it
		return JavaParser.parse(in);
	}

	@Test
	public void testRequestMapping() throws FileNotFoundException{
		
		String testFile = System.getProperty("user.dir") + "\\src\\test\\resources\\com\\aspectsecurity\\automation\\testing\\JavaParser\\test\\RequestMappingExample.java";

		CompilationUnit cu = generateCompilationUnitFromFile(testFile);

		logger.debug("Running visitors...");
		// visit and print the methods names
		cu.accept(new SpringAnnotationAnalyzer(), cu.getPackageDeclaration());
		
		//Get endpoints we've found
		ArrayList<Endpoint> endpoints = SpringAPIIdentifier.getEndpoints();
		
		logger.debug("Found: " + endpoints.size() + " endpoints.");
		assertEquals(endpoints.size(),7);
	}
	
	@Test
	public void testSpringConsumes() throws FileNotFoundException{
		String testFile = System.getProperty("user.dir") + "\\src\\test\\resources\\com\\aspectsecurity\\automation\\testing\\JavaParser\\test\\RequestMappingExample.java";

		CompilationUnit cu = generateCompilationUnitFromFile(testFile);

		logger.debug("Running visitors...");
		// visit and print the methods names
		cu.accept(new SpringAnnotationAnalyzer(), cu.getPackageDeclaration());
		
		//Get endpoints we've found
		ArrayList<Endpoint> endpoints = SpringAPIIdentifier.getEndpoints();
		
		assertEquals(endpoints.get(0).getConsumes().toString(),"[]");
		assertEquals(endpoints.get(1).getConsumes().toString(),"[application/json]");
		assertEquals(endpoints.get(2).getConsumes().toString(),"[MediaType.APPLICATION_JSON_VALUE]");
		//Note that arrays with multiple values have 2 spaces after ","
		assertEquals(endpoints.get(3).getConsumes().toString(),"[MediaType.APPLICATION_JSON_VALUE,  MediaType.APPLICATION_XML_VALUE]");
		assertEquals(endpoints.get(4).getConsumes().toString(),"[]");
		assertEquals(endpoints.get(5).getConsumes().toString(),"[]");
		assertEquals(endpoints.get(6).getConsumes().toString(),"[]");
	}
	
	@Test
	public void testSpringHeaders() throws FileNotFoundException{
		String testFile = System.getProperty("user.dir") + "\\src\\test\\resources\\com\\aspectsecurity\\automation\\testing\\JavaParser\\test\\RequestMappingExample.java";

		CompilationUnit cu = generateCompilationUnitFromFile(testFile);

		logger.debug("Running visitors...");
		// visit and print the methods names
		cu.accept(new SpringAnnotationAnalyzer(), cu.getPackageDeclaration());
		
		//Get endpoints we've found
		ArrayList<Endpoint> endpoints = SpringAPIIdentifier.getEndpoints();
		
		try{
			//If these do not throw an exception, we should fail, since they are empty
			fail(endpoints.get(0).getHeaders().get(0).getHttpParameterName());
		} catch(IndexOutOfBoundsException e){
			logger.debug("Excpetion thrown, as expected.");
		}
		try{
			//If these do not throw an exception, we should fail, since they are empty
			fail(endpoints.get(0).getHeaders().get(0).getDefaultValue());
		} catch(IndexOutOfBoundsException e){
			logger.debug("Excpetion thrown, as expected.");
		}
		try{
			//If these do not throw an exception, we should fail, since they are empty
			fail(endpoints.get(1).getHeaders().get(0).getHttpParameterName());
		} catch(IndexOutOfBoundsException e){
			logger.debug("Excpetion thrown, as expected.");
		}
		try{
			//If these do not throw an exception, we should fail, since they are empty
			fail(endpoints.get(1).getHeaders().get(0).getDefaultValue());

		} catch(IndexOutOfBoundsException e){
			logger.debug("Excpetion thrown, as expected.");
		}
		try{
			//If these do not throw an exception, we should fail, since they are empty
			fail(endpoints.get(2).getHeaders().get(0).getHttpParameterName());
		} catch(IndexOutOfBoundsException e){
			logger.debug("Excpetion thrown, as expected.");
		}
		try{
			//If these do not throw an exception, we should fail, since they are empty
			fail(endpoints.get(2).getHeaders().get(0).getDefaultValue());

		} catch(IndexOutOfBoundsException e){
			logger.debug("Excpetion thrown, as expected.");
		}
		try{
			//If these do not throw an exception, we should fail, since they are empty
			fail(endpoints.get(3).getHeaders().get(0).getHttpParameterName());
		} catch(IndexOutOfBoundsException e){
			logger.debug("Excpetion thrown, as expected.");
		}
		try{
			//If these do not throw an exception, we should fail, since they are empty
			fail(endpoints.get(3).getHeaders().get(0).getDefaultValue());
		} catch(IndexOutOfBoundsException e){
			logger.debug("Excpetion thrown, as expected.");
		}
		
		assertEquals(endpoints.get(4).getHeaders().get(0).getHttpParameterName(),"key1");
		assertEquals(endpoints.get(4).getHeaders().get(0).getDefaultValue(),"val1");
		assertEquals(endpoints.get(4).getHeaders().get(1).getHttpParameterName(),"key2");
		assertEquals(endpoints.get(4).getHeaders().get(1).getDefaultValue(),"val2");
		assertEquals(endpoints.get(5).getHeaders().get(0).getHttpParameterName(),"key1");
		assertEquals(endpoints.get(5).getHeaders().get(0).getDefaultValue(),"val1");
		assertEquals(endpoints.get(5).getHeaders().get(1).getHttpParameterName(),"key2");
		assertEquals(endpoints.get(5).getHeaders().get(1).getDefaultValue(),"val2");
		assertEquals(endpoints.get(6).getHeaders().get(0).getHttpParameterName(),"key");
		assertEquals(endpoints.get(6).getHeaders().get(0).getDefaultValue(),"val");
	}
	
	@Test
	public void testSpringHttpMethod() throws FileNotFoundException{
		String testFile = System.getProperty("user.dir") + "\\src\\test\\resources\\com\\aspectsecurity\\automation\\testing\\JavaParser\\test\\RequestMappingExample.java";

		CompilationUnit cu = generateCompilationUnitFromFile(testFile);

		logger.debug("Running visitors...");
		// visit and print the methods names
		cu.accept(new SpringAnnotationAnalyzer(), cu.getPackageDeclaration());
		
		//Get endpoints we've found
		ArrayList<Endpoint> endpoints = SpringAPIIdentifier.getEndpoints();
		
		assertEquals(endpoints.get(0).getMethods().toString(),"[]");
		assertEquals(endpoints.get(1).getMethods().toString(),"[RequestMethod.GET]");
		assertEquals(endpoints.get(2).getMethods().toString(),"[RequestMethod.GET]");
		assertEquals(endpoints.get(3).getMethods().toString(),"[RequestMethod.GET]");
		assertEquals(endpoints.get(4).getMethods().toString(),"[RequestMethod.POST]");
		assertEquals(endpoints.get(5).getMethods().toString(),"[RequestMethod.PUT,  RequestMethod.GET]");
		assertEquals(endpoints.get(6).getMethods().toString(),"[RequestMethod.POST,  RequestMethod.GET,  RequestMethod.PUT]");
	}
	
	@Test
	public void testSpringHttpProduces() throws FileNotFoundException{
		String testFile = System.getProperty("user.dir") + "\\src\\test\\resources\\com\\aspectsecurity\\automation\\testing\\JavaParser\\test\\RequestMappingExample.java";

		CompilationUnit cu = generateCompilationUnitFromFile(testFile);

		logger.debug("Running visitors...");
		// visit and print the methods names
		cu.accept(new SpringAnnotationAnalyzer(), cu.getPackageDeclaration());
		
		//Get endpoints we've found
		ArrayList<Endpoint> endpoints = SpringAPIIdentifier.getEndpoints();
		
		assertEquals(endpoints.get(0).getProduces().toString(),"[]");
		assertEquals(endpoints.get(1).getProduces().toString(),"[]");
		assertEquals(endpoints.get(2).getProduces().toString(),"[]");
		assertEquals(endpoints.get(3).getProduces().toString(),"[]");
		assertEquals(endpoints.get(4).getProduces().toString(),"[]");
		assertEquals(endpoints.get(5).getProduces().toString(),"[application/json,  application/xml]");
		assertEquals(endpoints.get(6).getProduces().toString(),"[application/json]");
	}
	
	@Test
	public void testSpringEndpointParams() throws FileNotFoundException{
		String testFile = System.getProperty("user.dir") + "\\src\\test\\resources\\com\\aspectsecurity\\automation\\testing\\JavaParser\\test\\SpringEndpointParametersExample.java";

		CompilationUnit cu = generateCompilationUnitFromFile(testFile);

		logger.debug("Running visitors...");
		// visit and print the methods names
		cu.accept(new SpringAnnotationAnalyzer(), cu.getPackageDeclaration());
		
		//Get endpoints we've found
		ArrayList<Endpoint> endpoints = SpringAPIIdentifier.getEndpoints();
				
		//public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
		assertEquals(endpoints.get(0).getParams().get(0).getHttpParameterName(),"name");
		assertEquals(endpoints.get(0).getParams().get(0).getCodeVariableName(),"name");
		assertEquals(endpoints.get(0).getParams().get(0).getType(),"String");
		assertEquals(endpoints.get(0).getParams().get(0).getDefaultValue(),"World");
		assertEquals(endpoints.get(0).getParams().get(0).getAnnotation(),"RequestParam");
		assertFalse(endpoints.get(0).getParams().get(0).isRequired());
		
		//public String endpoint11( @RequestParam("id9") String id9, @RequestParam("id10") String id10){
		assertEquals(endpoints.get(1).getParams().get(0).getHttpParameterName(),"id9");
		assertEquals(endpoints.get(1).getParams().get(0).getCodeVariableName(),"id9");
		assertEquals(endpoints.get(1).getParams().get(0).getType(),"String");
		assertEquals(endpoints.get(1).getParams().get(0).getDefaultValue(),"");
		assertEquals(endpoints.get(1).getParams().get(0).getAnnotation(),"RequestParam");
		assertFalse(endpoints.get(1).getParams().get(0).isRequired());
		
		//public String endpoint11( @RequestParam("id9") String id9, @RequestParam("id10") String id10){
		assertEquals(endpoints.get(1).getParams().get(1).getHttpParameterName(),"id10");
		assertEquals(endpoints.get(1).getParams().get(1).getCodeVariableName(),"id10");
		assertEquals(endpoints.get(1).getParams().get(1).getType(),"String");
		assertEquals(endpoints.get(1).getParams().get(1).getDefaultValue(),"");
		assertEquals(endpoints.get(1).getParams().get(1).getAnnotation(),"RequestParam");
		assertFalse(endpoints.get(1).getParams().get(1).isRequired());
		
		//these should throw exceptions since there are no params for this endpoint
		//public String index() {
		try{
			fail(endpoints.get(2).getParams().get(0).getHttpParameterName());
		} catch(IndexOutOfBoundsException e){
			logger.debug("Excpetion thrown, as expected.");
		}
		try{
			fail(endpoints.get(2).getParams().get(0).getCodeVariableName());
		} catch(IndexOutOfBoundsException e){
			logger.debug("Excpetion thrown, as expected.");
		}
		try{
			fail(endpoints.get(2).getParams().get(0).getType());
		} catch(IndexOutOfBoundsException e){
			logger.debug("Excpetion thrown, as expected.");
		}
		try{
			fail(endpoints.get(2).getParams().get(0).getDefaultValue());
		} catch(IndexOutOfBoundsException e){
			logger.debug("Excpetion thrown, as expected.");
		}
		try{
			fail(endpoints.get(2).getParams().get(0).getAnnotation());
		} catch(IndexOutOfBoundsException e){
			logger.debug("Excpetion thrown, as expected.");
		}
		
		//public String endpoint13( @PathVariable("id1") String id1){
		assertEquals(endpoints.get(3).getParams().get(0).getHttpParameterName(),"id1");
		assertEquals(endpoints.get(3).getParams().get(0).getCodeVariableName(),"id1");
		assertEquals(endpoints.get(3).getParams().get(0).getType(),"String");
		assertEquals(endpoints.get(3).getParams().get(0).getDefaultValue(),"");
		assertEquals(endpoints.get(3).getParams().get(0).getAnnotation(),"PathVariable");
		assertFalse(endpoints.get(3).getParams().get(0).isRequired());
		
		//public Greeting endpoint15(@RequestParam(value="name", defaultValue="World", required=true) String name) {
		assertEquals(endpoints.get(4).getParams().get(0).getHttpParameterName(),"name");
		assertEquals(endpoints.get(4).getParams().get(0).getCodeVariableName(),"name");
		assertEquals(endpoints.get(4).getParams().get(0).getType(),"String");
		assertEquals(endpoints.get(4).getParams().get(0).getDefaultValue(),"World");
		assertEquals(endpoints.get(4).getParams().get(0).getAnnotation(),"RequestParam");
		assertTrue(endpoints.get(4).getParams().get(0).isRequired());
		
		
		
	}
	
}
