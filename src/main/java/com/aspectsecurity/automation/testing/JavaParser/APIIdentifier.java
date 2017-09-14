package com.aspectsecurity.automation.testing.JavaParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.expr.MemberValuePair;

public class APIIdentifier {

	private static ArrayList<Endpoint> endpoints;

	public static void main(String[] args) throws FileNotFoundException {
		Logger logger = LoggerFactory.getLogger(APIIdentifier.class);
		endpoints = new ArrayList<Endpoint>();

		// Assumes this file is part of this project
		FileInputStream in = new FileInputStream(System.getProperty("user.dir")
				+ "\\src\\test\\resources\\com\\aspectsecurity\\automation\\testing\\JavaParser\\test\\Endpoint2.java");

		// parse it
		CompilationUnit cu = JavaParser.parse(in);

		// visit and print the methods names
		cu.accept(new MethodVisitor(), cu.getPackageDeclaration());

		logger.debug("Printing Endpoint Info:");
		for (Endpoint endpoint : endpoints) {
			logger.info("=====================================================");
			logger.info("Name: " + endpoint.getName());
			logger.info("URL: " + endpoint.getUrl());
			logger.info("HTTP Methods: " + endpoint.getMethods().toString());
			logger.info("Consumes: " + endpoint.getConsumes().toString());
			logger.info("Produces: " + endpoint.getProduces().toString());
			logger.info("Part of class: "+ endpoint.getClazzName());
			logger.info("Headers:");
			for (Parameter header : endpoint.getHeaders()){
				 logger.info("\t\t" + header.getName() + " = " + header.getValue());
			}
		}

	}

	public ArrayList<Endpoint> getEndpointsFromSpring() {

		return endpoints;
	}

	/**
	 * Simple visitor implementation for visiting MethodDeclaration nodes.
	 */
	static class MethodVisitor extends VoidVisitorAdapter<Optional<PackageDeclaration>> {
		Logger logger = LoggerFactory.getLogger(MethodVisitor.class);

		@Override
		public void visit(MethodDeclaration n, Optional<PackageDeclaration> clazzPackage) {

			/* CHECK METHOD-LEVEL ANNOTATIONS FOR URL AND HTTP METHOD */

			// We found a new method to look at
			logger.debug("Method Name: " + n.getName());
			String methodClazzName = getClassNameFromMethod(n);

			// Get all annotations on method
			NodeList<AnnotationExpr> nodeList = n.getAnnotations();
			for (AnnotationExpr annotation : nodeList) {

				// Found an annotation on the method
				logger.debug("Found annotation: " + annotation.getNameAsString());
				if (annotation.getNameAsString().equals("RequestMapping")) {
					handleRequestMappingFound(annotation, clazzPackage.get().getName().toString(), methodClazzName);
				}
			}

			super.visit(n, clazzPackage);
		}
		
		private String getClassNameFromMethod(MethodDeclaration n) {
			Optional<ClassOrInterfaceDeclaration> methodClazzNode = null;
			String methodClazzName = null;

			logger.debug("Getting class name");
			// Get the name of the class this method belongs to
			methodClazzNode = n.getAncestorOfType(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration.class);
			methodClazzName = methodClazzNode.get().getName().toString();

			if (methodClazzName != null) {
				logger.debug("Found class: " + methodClazzName);
			} else {
				logger.debug("Did not find class name.");
			}

			return methodClazzName;
		}

		private List<String> handleMemberValuePairSet(MemberValuePair parentNode) {

			return Arrays.asList(parentNode.getChildNodes().get(0).toString().replaceAll("\"", ""),
					parentNode.getChildNodes().get(1).toString().replaceAll("\"", ""));

		}

		private void handleRequestMappingFound(AnnotationExpr annotation, String clazzPackageName,
				String methodClazzName) {
			// This usually means we have a new endpoint
			Endpoint newEndpoint = new Endpoint();
			for (Node annotationAttribute : annotation.getChildNodes()) {
				// Look for parameters passed into the annotation [ex.
				// @RequestMapping("/endpoint2")]

				// This check makes sure we don't add the annotation
				// name itself (ex. RequestMapping) to the URL list
				if (annotationAttribute.getClass().isInstance(new com.github.javaparser.ast.expr.Name())) {
					logger.debug("Skipping...");
					continue;
				}
				// Onward to discover what the parameter/attribute to
				// the annotation is
				logger.debug("");
				ArrayList<AnnotationAttribute> annotationAttributes = new ArrayList<AnnotationAttribute>();
				logger.debug("Annotation Attribute: " + annotationAttribute.toString());

				// We'll check if it's a key-value pair, since we have
				// to make sure we store those together
				if (annotationAttribute.getClass().isInstance(new com.github.javaparser.ast.expr.MemberValuePair())) {
					logger.debug("KeyPair Found...");
					List<String> newAttribute = handleMemberValuePairSet((MemberValuePair) annotationAttribute);
					logger.debug("Adding attribute: " + newAttribute.get(0) + " = " + newAttribute.get(1));
					annotationAttributes.add(new AnnotationAttribute(newAttribute.get(0), newAttribute.get(1)));

				} else { // single attribute, not key-value pair; assume URL? If
							// not, we'll have to re-work this logic to not
							// store as "value"
					logger.debug("Keypair not found...");
					annotationAttributes.add(new AnnotationAttribute("value", annotationAttribute.toString().trim()));
				}

				logger.debug("");
				// Cycle though our AnnotationAttributes and identify what each
				// thing is, then store it appropriately
				for (AnnotationAttribute attribute : annotationAttributes) {
					logger.debug("Name: " + attribute.getName());
					logger.debug("Value: " + attribute.getValue());

					// Set the class of the method, so we can later reconcile
					// method vs class-level annotations
					logger.debug("Adding class: " + clazzPackageName + "." + methodClazzName + ".class");
					newEndpoint.setClazzName(clazzPackageName + "." + methodClazzName + ".class");

					if (attribute.getName().equals("value") || attribute.getName().equals("path")) {
						// "value" = URL
						logger.debug("Adding URL: " + attribute.getValue());
						newEndpoint.setUrl(attribute.getValue().replaceAll("\"", "").trim());
						logger.debug("added url: " + newEndpoint.getUrl());

					} else if (attribute.getName().equals("method")) {
						// "method" = HTTP method
						
						String[] methods = new String[10]; // capping at 10 methods arbitrarily...
						methods[0] = attribute.getValue().replaceAll("\"", "").trim(); //Stick whatever we have in an array - if it's just one method, we're good. If not, we'll replace the array contents later
						
						if(attribute.getValue().trim().startsWith("{")){ //multiple methods to add
							methods = attribute.getValue().replaceAll("\\{", "").replaceAll("\\}", "").trim().split(","); //remove array notation (ex. { item1, item2 }) and put each item in its own index in "methods"
						} 
							for(String httpMethod : methods){
								if(httpMethod != null){ //the array will have a bunch of empty spots, unless 10 HTTP Methods are supported
									logger.debug("Adding Method: " + httpMethod);
									newEndpoint.addMethod(httpMethod);
									logger.debug("added method: " + httpMethod);
								}
							}
						
					} else if (attribute.getName().equals("headers")) {
						//"headers" = headers
						
						logger.debug("Found at least one header... " + attribute.getValue());
						Parameter[] headers = new Parameter[10]; // capping at 10 headers arbitrarily...
						if(!attribute.getValue().contains("=")){ //if there is no "=" something is probably wrong (header with a name, but no value), but we should handle it anyway
							logger.debug("Didn't find an \"=\" in header assignment. Weird.");
							headers[0]=new Parameter(attribute.getValue().replaceAll("\"", ""), "");
						} else {
							logger.debug("Found at least one header assignment.");
							//Stick whatever we have in an array - if it's just one header, we're good. If not, we'll replace the array contents later
							headers[0] = new Parameter(attribute.getValue().replaceAll("\"", "").trim().split("=")[0], attribute.getValue().replaceAll("\"", "").trim().split("=")[1]);
							logger.debug("Attempting to add header: " + headers[0].getName() + " = " + headers[0].getValue());
						}
						//remove array notation (ex. { item1=blah, item2=blah2 }) and put each item in its own index in "headers"
						if(attribute.getValue().trim().startsWith("{")){ //multiple headers to add
							String[] tempArray = attribute.getValue().replaceAll("\\{", "").replaceAll("\\}", "").trim().split(","); 
							
							for(int i = 0; i < tempArray.length; i++){
								headers[i] = new Parameter(tempArray[i].split("=")[0].trim(), tempArray[i].split("=")[1].trim());
								logger.debug("Setting header: " + headers[i].getName() + " = " + headers[i].getValue());
							}
						} 
							for(Parameter httpHeader : headers){
								if(httpHeader != null){ //the array will have a bunch of empty spots, unless 10 HTTP Methods are supported
									logger.debug("Adding Header: " + httpHeader.getName() + " = " + httpHeader.getValue());
									newEndpoint.addHeaders(httpHeader);
									logger.debug("Added Header: " + httpHeader.getName() + " = " + httpHeader.getValue());
								}
							}
					}  else if (attribute.getName().equals("produces")) {
						// "produces" = output format
						
						String[] produces = new String[10]; // capping at 10 output formats arbitrarily...
						produces[0] = attribute.getValue().replaceAll("\"", "").trim(); //Stick whatever we have in an array - if it's just one produces, we're good. If not, we'll replace the array contents later
						
						//remove array notation (ex. { item1, item2 }) and put each item in its own index in "produces"
						if(attribute.getValue().trim().startsWith("{")){ //multiple "produces" to add
							produces = attribute.getValue().replaceAll("\\{", "").replaceAll("\\}", "").trim().split(","); 
						} 
							for(String outputFormat : produces){
								if(outputFormat != null){ //the array will have a bunch of empty spots, unless 10 output formats are supported
									logger.debug("Adding Method: " + outputFormat);
									newEndpoint.addProduces(outputFormat);
									logger.debug("added method: " + outputFormat);
								}
							}
						
					}  else if (attribute.getName().equals("consumes")) {
						// "consumes" = ingest format
						
						String[] consumes = new String[10]; // capping at 10 ingest formats arbitrarily...
						consumes[0] = attribute.getValue().replaceAll("\"", "").trim(); //Stick whatever we have in an array - if it's just one consumes, we're good. If not, we'll replace the array contents later
						
						//remove array notation (ex. { item1, item2 }) and put each item in its own index in "consumes"
						if(attribute.getValue().trim().startsWith("{")){ //multiple "consumes" to add
							consumes = attribute.getValue().replaceAll("\\{", "").replaceAll("\\}", "").trim().split(","); 
						} 
							for(String ingestFormat : consumes){
								if(ingestFormat != null){ //the array will have a bunch of empty spots, unless 10 ingest formats are supported
									logger.debug("Adding Method: " + ingestFormat);
									newEndpoint.addConsumes(ingestFormat);
									logger.debug("added method: " + ingestFormat);
								}
							}
					
					} else if (attribute.getName().equals("name")) {
						// "name" = Name
						logger.debug("Adding Name: " + attribute.getValue());
						newEndpoint.setName(attribute.getValue().replaceAll("\"", "").trim());
						logger.debug("added Name: " + newEndpoint.getName());
					}
				}

			}
			// Add all our endpoints and associated data to an array because idk
			// how to return it...
			endpoints.add(newEndpoint);
			logger.debug("");
			logger.debug("-----------------------------------------------");
			logger.debug("");
		}


		
	}
	/*
	 * 
	 * 
	 * From: https://stormpath.com/blog/jax-rs-vs-spring-rest-endpoints Spring
	 * Annotation JAX-RS Annotation
	 * 
	 * @RequestMapping(path = "/troopers" @Path("/troopers")
	 * 
	 * @RequestMapping(method = RequestMethod.POST) @POST
	 * 
	 * @RequestMapping(method = RequestMethod.GET) @GET
	 * 
	 * @RequestMapping(method = RequestMethod.DELETE) @DELETE
	 * 
	 * @ResponseBody N/A
	 * 
	 * @RequestBody N/A
	 * 
	 * @PathVariable("id") @PathParam("id")
	 * 
	 * @RequestParam("xyz") @QueryParam('xyz")
	 * 
	 * @RequestParam(value="xyz" @FormParam(“xyz”)
	 * 
	 * @RequestMapping(produces =
	 * {"application/json"}) @Produces("application/json")
	 * 
	 * @RequestMapping(consumes = {"application/json"}) @Consumes("application/json") Wrapping things up
	 * 
	 * 
	 * 
	 */
}
