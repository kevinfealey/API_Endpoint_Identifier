package com.aspectsecurity.automation.testing.JavaParser.visitors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectsecurity.automation.testing.JavaParser.SpringAPIIdentifier;
import com.aspectsecurity.automation.testing.JavaParser.objects.AnnotationAttribute;
import com.aspectsecurity.automation.testing.JavaParser.objects.Endpoint;
import com.aspectsecurity.automation.testing.JavaParser.objects.Parameter;
import com.aspectsecurity.automation.testing.JavaParser.parsers.ConsumesParsers;
import com.aspectsecurity.automation.testing.JavaParser.parsers.HeaderParsers;
import com.aspectsecurity.automation.testing.JavaParser.parsers.HttpMethodParsers;
import com.aspectsecurity.automation.testing.JavaParser.parsers.ProducesParsers;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
	 * Simple visitor implementation for visiting MethodDeclaration nodes.
	 */
	public class SpringAnnotationAnalyzer extends VoidVisitorAdapter<Optional<PackageDeclaration>> {
		Logger logger = LoggerFactory.getLogger(SpringAnnotationAnalyzer.class);

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

		//Handles everything to do with the @RequestMapping annotation in Spring
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
						
						logger.debug("Found at least one method... " + attribute.getValue());
						String[] methods = HttpMethodParsers.parseSpringRequestMappingHttpMethods(attribute);
						for(String httpMethod : methods){
							if(httpMethod != null){ //the array will have a bunch of empty spots, unless 10 HTTP Methods are supported
								logger.debug("Adding Method: " + httpMethod);
								newEndpoint.addMethod(httpMethod);
								logger.debug("Added Method: " + httpMethod);
							}
						}
						
					} else if (attribute.getName().equals("headers")) {
						//"headers" = headers
						
						logger.debug("Found at least one header... " + attribute.getValue());
						Parameter[] headers = HeaderParsers.parseSpringRequestMappingHeaders(attribute);
						for(Parameter httpHeader : headers){
							if(httpHeader != null){ //the array will have a bunch of empty spots, unless 10 HTTP Methods are supported
								logger.debug("Adding Header: " + httpHeader.getName() + " = " + httpHeader.getValue());
								newEndpoint.addHeaders(httpHeader);
								logger.debug("Added Header: " + httpHeader.getName() + " = " + httpHeader.getValue());
							}
						}
						
					}  else if (attribute.getName().equals("produces")) {
						// "produces" = output format
						logger.debug("Found at least one produces... " + attribute.getValue());
						String[] produces = ProducesParsers.parseSpringRequestMappingProduces(attribute);
						for(String outputFormat : produces){
							if(outputFormat != null){ //the array will have a bunch of empty spots, unless 10 output formats are supported
								logger.debug("Adding Produces: " + outputFormat);
								newEndpoint.addProduces(outputFormat);
								logger.debug("Added Produces: " + outputFormat);
							}
						}
						
					}  else if (attribute.getName().equals("consumes")) {
						// "consumes" = ingest format
						
						String[] consumes = ConsumesParsers.parseSpringRequestMappingConsumes(attribute);
						for(String ingestFormat : consumes){
							if(ingestFormat != null){ //the array will have a bunch of empty spots, unless 10 ingest formats are supported
								logger.debug("Adding Consumes: " + ingestFormat);
								newEndpoint.addConsumes(ingestFormat);
								logger.debug("Added Consumes: " + ingestFormat);
							}
						}
					
					} else if (attribute.getName().equals("name")) {
						// "name" = Name
						logger.debug("Adding Name: " + attribute.getValue());
						newEndpoint.setName(attribute.getValue().replaceAll("\"", "").trim());
						logger.debug("Added Name: " + newEndpoint.getName());
					}
				}

			}
			// Add all our endpoints and associated data to an array in SpringAPIIdentifier because idk
			// how to return it... we should look for a better option that storing in this static object
			SpringAPIIdentifier.addEndpoint(newEndpoint);
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
