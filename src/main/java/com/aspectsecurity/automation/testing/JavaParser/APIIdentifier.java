package com.aspectsecurity.automation.testing.JavaParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.expr.MemberValuePair;

public class APIIdentifier {

	private static ArrayList<Endpoint> endpoints;

	public static void main(String[] args) throws FileNotFoundException {
		Logger logger = LoggerFactory.getLogger(APIIdentifier.class);
		endpoints = new ArrayList<Endpoint>();

		//Assumes this file is part of this project
		FileInputStream in = new FileInputStream(System.getProperty("user.dir")+
				"\\src\\test\\resources\\com\\aspectsecurity\\automation\\testing\\JavaParser\\test\\Endpoint2.java");
				
		// parse it
		CompilationUnit cu = JavaParser.parse(in);

		// visit and print the methods names
		cu.accept(new MethodVisitor(), null);

		logger.debug("Printing Endpoint Info:");
		for (Endpoint endpoint : endpoints) {
			logger.info("URL: " + endpoint.getUrl() + " | " + "Method: " + endpoint.getMethod());
		}

	}
	
	public ArrayList<Endpoint> getEndpointsFromSpring(){
		
		return endpoints;
	}

	/**
	 * Simple visitor implementation for visiting MethodDeclaration nodes.
	 */
	static class MethodVisitor extends VoidVisitorAdapter<Void> {
		private void getURLs(MethodDeclaration n, Void arg) {

		}

		@Override
		public void visit(MethodDeclaration n, Void arg) {
			Logger logger = LoggerFactory.getLogger(MethodVisitor.class);
			/* CHECK METHOD-LEVEL ANNOTATIONS FOR URL AND HTTP METHOD */
			// We found a new method to look at
			logger.debug("Method Name: " + n.getName());

			// Get all annotations on method
			NodeList<AnnotationExpr> nodeList = n.getAnnotations();
			for (AnnotationExpr annotation : nodeList) {

				// Found an annotation on the method
				logger.debug("Found annotation: " + annotation.getNameAsString());
				if (annotation.getNameAsString().equals("RequestMapping")) {

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
						if (annotationAttribute.getClass()
								.isInstance(new com.github.javaparser.ast.expr.MemberValuePair())) {
							logger.debug("KeyPair Found...");

							// There shouldn't be more than 2 items in a
							// key-value pair
							if (annotationAttribute.getChildNodes().size() != 2) {
								logger.debug("Something isn't right...");
							} else {
								logger.debug("0: " + annotationAttribute.getChildNodes().get(0));
								logger.debug("1: " + annotationAttribute.getChildNodes().get(1));
								
								//Store our key-value pair (with no quotes or front/end whitespace) as an Annotation Attribute
								annotationAttributes.add(new AnnotationAttribute(
										annotationAttribute.getChildNodes().get(0).toString().replaceAll("\"", "")
												.trim(),
										annotationAttribute.getChildNodes().get(1).toString().replaceAll("\"", "")
												.trim()));
							}

						} else { // single attribute, not key-value pair; assume URL? If not, we'll have to re-work this logic to not store as "value"
							logger.debug("Keypair not found...");
							annotationAttributes
									.add(new AnnotationAttribute("value", annotationAttribute.toString().trim()));
						}
						
						logger.debug("");
						//Cycle though our AnnotationAttributes and identify what each thing is, then store it appropriately
						for (AnnotationAttribute attribute : annotationAttributes) {
							logger.debug("Name: " + attribute.getName());
							logger.debug("Value: " + attribute.getValue());

							if (attribute.getName().equals("value")) {
								//"value" = URL
								logger.debug("Adding URL: " + attribute.getValue());
								newEndpoint.setUrl(attribute.getValue().replaceAll("\"", "").trim());
								logger.debug("added url: " + newEndpoint.getUrl());
							

							} else if (attribute.getName().equals("method")) {
								//"method" = HTTP method
								logger.debug("Adding Method: " + attribute.getValue());
								newEndpoint.setMethod(attribute.getValue().replaceAll("\"", "").trim());
								logger.debug("added method: " + newEndpoint.getMethod());
							}
						}

					}
					//Add all our endpoints and associated data to an array because idk how to return it...
					endpoints.add(newEndpoint);
					logger.debug("");
					logger.debug("-----------------------------------------------");
					logger.debug("");
				}

			}

			super.visit(n, arg);
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
	 * @RequestMapping(consumes =
	 * {"application/json"}) @Consumes("application/json") Wrapping things up
	 * 
	 * 
	 * 
	 */
}
