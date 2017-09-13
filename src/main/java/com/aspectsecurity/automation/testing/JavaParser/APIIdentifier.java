package com.aspectsecurity.automation.testing.JavaParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

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
		endpoints = new ArrayList<Endpoint>();
		// creates an input stream for the file to be parsed
		FileInputStream in = new FileInputStream(
				"D:\\API_Tester\\demo_apps\\gs-spring-boot\\complete\\src\\main\\java\\hello\\Endpoint2.java");

		// parse it
		CompilationUnit cu = JavaParser.parse(in);

		// visit and print the methods names
		cu.accept(new MethodVisitor(), null);
		
		System.out.println("Printing Endpoint Info:");
		for (Endpoint endpoint : endpoints) {
			System.out.println("URL: " + endpoint.getUrl() + " | " + "Method: " + endpoint.getMethod());
		}

	}

	/**
	 * Simple visitor implementation for visiting MethodDeclaration nodes.
	 */
	private static class MethodVisitor extends VoidVisitorAdapter<Void> {
		private void getURLs(MethodDeclaration n, Void arg){
			
		}
		
		@Override
		public void visit(MethodDeclaration n, Void arg) {
			/* CHECK METHOD-LEVEL ANNOTATIONS FOR URL AND HTTP METHOD */
			System.out.println("Method Name: " + n.getName());
			NodeList<AnnotationExpr> nodeList = n.getAnnotations();
			for (AnnotationExpr annotation : nodeList) {
				System.out.println("Found annotation: " + annotation.getNameAsString());
				if (annotation.getNameAsString().equals("RequestMapping")) {
					Endpoint newEndpoint = new Endpoint();
					for (Node annotationAttribute : annotation.getChildNodes()) {
						System.out.println("Class name: " + annotationAttribute.getClass().getName());
						//This check makes sure we don't add the annotation name itself (ex. RequestMapping) to the URL list
						if(annotationAttribute.getClass().isInstance(new com.github.javaparser.ast.expr.Name())){
							System.out.println("Skipping...");
							continue;
						}
						System.out.println("");
						ArrayList<AnnotationAttribute> annotationAttributes = new ArrayList<AnnotationAttribute>();
						System.out.println("Annotation Attribute: " + annotationAttribute.toString());
						if(annotationAttribute.getClass().isInstance(new com.github.javaparser.ast.expr.MemberValuePair())){ // key-value pairs
							System.out.println("KeyPair Found...");
							if(annotationAttribute.getChildNodes().size() != 2){
								System.out.println("Something isn't right...");
							} else {
								System.out.println("0: " + annotationAttribute.getChildNodes().get(0));
								System.out.println("1: " + annotationAttribute.getChildNodes().get(1));
								annotationAttributes.add(new AnnotationAttribute(annotationAttribute.getChildNodes().get(0).toString().replaceAll("\"", "").trim(), annotationAttribute.getChildNodes().get(1).toString().replaceAll("\"", "").trim()));
							}
							
						} else { // single attribute, not key-value pair; assume URL?
							System.out.println("Keypair not found...");
							annotationAttributes.add(new AnnotationAttribute("value", annotationAttribute.toString().trim()));
						}
						System.out.println("");
						for (AnnotationAttribute attribute : annotationAttributes) {
							System.out.println("Name: " + attribute.getName());
							System.out.println("Value: " + attribute.getValue());
							if (attribute.getName().equals("value")) {
								System.out.println("Adding URL: " + attribute.getValue());
								newEndpoint.setUrl(attribute.getValue().replaceAll("\"", "").trim());
								System.out.println("added url: " + newEndpoint.getUrl());
							} else if (attribute.getName().equals("method")){
								System.out.println("Adding Method: " + attribute.getValue());
								newEndpoint.setMethod(attribute.getValue().replaceAll("\"", "").trim());
								System.out.println("added method: " + newEndpoint.getMethod());
							}
						}
						
					}
					endpoints.add(newEndpoint);
					System.out.println("");
					System.out.println("-----------------------------------------------");
					System.out.println("");
				}

			}

			super.visit(n, arg);
		}
	}
/*
 * 
 * 
 * From: https://stormpath.com/blog/jax-rs-vs-spring-rest-endpoints
 * Spring Annotation	JAX-RS Annotation
@RequestMapping(path = "/troopers"	@Path("/troopers")
@RequestMapping(method = RequestMethod.POST)	@POST
@RequestMapping(method = RequestMethod.GET)	@GET
@RequestMapping(method = RequestMethod.DELETE)	@DELETE
@ResponseBody	N/A
@RequestBody	N/A
@PathVariable("id")	@PathParam("id")
@RequestParam("xyz")	@QueryParam('xyz")
@RequestParam(value="xyz"	@FormParam(“xyz”)
@RequestMapping(produces = {"application/json"})	@Produces("application/json")
@RequestMapping(consumes = {"application/json"})	@Consumes("application/json")
Wrapping things up
 * 
 * 
 * 
 */
}
