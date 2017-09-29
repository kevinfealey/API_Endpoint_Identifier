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
    private Logger logger = LoggerFactory.getLogger(SpringAnnotationAnalyzer.class);

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
                String packageName = "";
                if (clazzPackage.isPresent()) {
                    packageName = clazzPackage.get().getNameAsString();
                }

                Endpoint newEndpoint = handleRequestMappingFound(annotation, packageName, methodClazzName);

                // Check method parameters since we have a RequestMapping
                newEndpoint.setParams(handleMethodParameters(n.getParameters()));

                SpringAPIIdentifier.addEndpoint(newEndpoint);
            }
        }

        super.visit(n, clazzPackage);
    }

    private ArrayList<Parameter> handleMethodParameters(NodeList<com.github.javaparser.ast.body.Parameter> params) {
        ArrayList<Parameter> parameters = new ArrayList<>();

        for (com.github.javaparser.ast.body.Parameter param : params) {
            Parameter myParam = new Parameter();

            logger.debug("PARAMETER NAME: " + param.getName());
            logger.debug("PARAMETER TYPE: " + param.getType());

            myParam.setCodeVariableName(param.getName().toString());
            myParam.setType(param.getType().toString());
            myParam.setDefaultValue("");
            myParam.setRequired(false);

            // Look at annotations that augment our method parameters
            NodeList<AnnotationExpr> annots = param.getAnnotations();
            for (AnnotationExpr annot : annots) {
                // This returns something like: @RequestParam("id10")
                logger.debug("ANNOTATION ON PARAM: " + annot.toString());

                // Look at parameters to the annotation
                List<Node> annotChildren = annot.getChildNodes();
                for (Node child : annotChildren) {
                    if (child.getClass().equals(com.github.javaparser.ast.expr.Name.class)) {

                        // This is the type of annotation
                        myParam.setAnnotation(child.toString().replaceAll("\"", ""));
                    } else if (child.getClass().equals(com.github.javaparser.ast.expr.StringLiteralExpr.class)) {
                        logger.debug("Found HTTP Parameter: " + child.toString());
                        myParam.setHttpParameterName(child.toString().replaceAll("\"", ""));

                    } else if (child.getClass().equals(com.github.javaparser.ast.expr.MemberValuePair.class)) {
                        List<String> memValPair = handleMemberValuePairSet((MemberValuePair) child);

                        switch (memValPair.get(0)) {
                            case "value":
                                myParam.setHttpParameterName(memValPair.get(1).replaceAll("\"", ""));
                                break;
                            case "defaultValue":
                                myParam.setDefaultValue(memValPair.get(1).replaceAll("\"", ""));
                                break;
                            case "required":
                                myParam.setRequired(Boolean.valueOf(memValPair.get(1).replaceAll("\"", "")));
                                break;
                        }

                    } else if (child.getClass().toString().equals("com.github.javaparser.ast.expr.NameExpr")) {
                        logger.debug("Name expression found in variable. We cannot determine the variable value at this time.");
                    }

                    logger.debug("Annotation Child: " + child.getClass());
                }
            }

            parameters.add(myParam);
        }

        return parameters;
    }

    public String getClassNameFromMethod(MethodDeclaration n) {
        Optional<ClassOrInterfaceDeclaration> methodClazzNode;
        String methodClazzName = null;

        logger.debug("Getting class name");

        // Get the name of the class this method belongs to
        methodClazzNode = n.getAncestorOfType(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration.class);
        if (methodClazzNode.isPresent()) {
            methodClazzName = methodClazzNode.get().getNameAsString();
        }

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

    // Handles everything to do with the @RequestMapping annotation in Spring
    private Endpoint handleRequestMappingFound(AnnotationExpr annotation, String clazzPackageName, String methodClazzName) {

        // This usually means we have a new endpoint
        Endpoint newEndpoint = new Endpoint();

        // Look for parameters passed into the annotation [ex. @RequestMapping("/endpoint2")]
        for (Node annotationAttribute : annotation.getChildNodes()) {

            // This check makes sure we don't add the annotation name itself (ex. RequestMapping) to the URL list
            if (annotationAttribute.getClass().isInstance(new com.github.javaparser.ast.expr.Name())) {
                logger.debug("Skipping...");
                continue;
            }

            // Onward to discover what the parameter/attribute to the annotation is
            logger.debug("");
            ArrayList<AnnotationAttribute> annotationAttributes = new ArrayList<>();
            logger.debug("Annotation Attribute: " + annotationAttribute.toString());

            // We'll check if it's a key-value pair, since we have to make sure we store those together
            if (annotationAttribute.getClass().isInstance(new com.github.javaparser.ast.expr.MemberValuePair())) {
                logger.debug("KeyPair Found...");
                List<String> newAttribute = handleMemberValuePairSet((MemberValuePair) annotationAttribute);
                logger.debug("Adding attribute: " + newAttribute.get(0) + " = " + newAttribute.get(1));
                annotationAttributes.add(new AnnotationAttribute(newAttribute.get(0), newAttribute.get(1)));
            } else {
                // Single attribute, not key-value pair; assume URL?
                // If not, we'll have to re-work this logic to not store as "value"
                logger.debug("Keypair not found...");
                annotationAttributes.add(new AnnotationAttribute("value", annotationAttribute.toString().trim()));
            }

            logger.debug("");

            // Cycle though AnnotationAttributes, identify what each thing is, then store appropriately
            for (AnnotationAttribute attribute : annotationAttributes) {
                logger.debug("Name: " + attribute.getName());
                logger.debug("Value: " + attribute.getValue());

                // Set the class of the method, so we can later reconcile method vs. class-level annotations
                logger.debug("Adding class: " + clazzPackageName + "." + methodClazzName + ".class");
                newEndpoint.setClazzName(clazzPackageName + "." + methodClazzName + ".class");

                switch (attribute.getName()) {
                    case "value":
                    case "path":

                        // "value" = URL
                        logger.debug("Adding URL: " + attribute.getValue());
                        newEndpoint.setUrl(attribute.getValue().replaceAll("\"", "").trim());
                        logger.debug("added url: " + newEndpoint.getUrl());
                        break;
                    case "method":

                        // "method" = HTTP method
                        logger.debug("Found at least one method... " + attribute.getValue());
                        String[] methods = HttpMethodParsers.parseSpringRequestMappingHttpMethods(attribute);
                        for (String httpMethod : methods) {
                            if (httpMethod != null) {
                                // Array will have a bunch of empty spots, unless 10 HTTP methods are supported
                                logger.debug("Adding Method: " + httpMethod);
                                newEndpoint.addMethod(httpMethod);
                                logger.debug("Added Method: " + httpMethod);
                            }
                        }
                        break;
                    case "headers":

                        // "headers" = headers
                        logger.debug("Found at least one header... " + attribute.getValue());
                        Parameter[] headers = HeaderParsers.parseSpringRequestMappingHeaders(attribute);
                        for (Parameter httpHeader : headers) {
                            if (httpHeader != null) {
                                // Array will have a bunch of empty spots, unless 10 HTTP methods are supported
                                logger.debug("Adding Header: " + httpHeader.getHttpParameterName() + " = " + httpHeader.getDefaultValue());
                                newEndpoint.addHeaders(httpHeader);
                                logger.debug("Added Header: " + httpHeader.getHttpParameterName() + " = " + httpHeader.getDefaultValue());
                            }
                        }
                        break;
                    case "produces":

                        // "produces" = output format
                        logger.debug("Found at least one produces... " + attribute.getValue());
                        String[] produces = ProducesParsers.parseSpringRequestMappingProduces(attribute);
                        for (String outputFormat : produces) {
                            if (outputFormat != null) {

                                // Array will have a bunch of empty spots, unless 10 HTTP methods are supported
                                logger.debug("Adding Produces: " + outputFormat);
                                newEndpoint.addProduces(outputFormat);
                                logger.debug("Added Produces: " + outputFormat);
                            }
                        }
                        break;
                    case "consumes":

                        // "consumes" = ingest format
                        logger.debug("Found at least one consumes... " + attribute.getValue());
                        String[] consumes = ConsumesParsers.parseSpringRequestMappingConsumes(attribute);
                        for (String ingestFormat : consumes) {
                            if (ingestFormat != null) {

                                // Array will have a bunch of empty spots, unless 10 HTTP methods are supported
                                logger.debug("Adding Consumes: " + ingestFormat);
                                newEndpoint.addConsumes(ingestFormat);
                                logger.debug("Added Consumes: " + ingestFormat);
                            }
                        }
                        break;
                    case "name":

                        // "name" = Name
                        logger.debug("Adding Name: " + attribute.getValue());
                        newEndpoint.setName(attribute.getValue().replaceAll("\"", "").trim());
                        logger.debug("Added Name: " + newEndpoint.getName());
                        break;
                }
            }
        }

        // Add all our endpoints and associated data to an array in SpringAPIIdentifier because idk how to
        // return it... we should look for a better option that storing in this static object

//		SpringAPIIdentifier.addEndpoint(newEndpoint);

        logger.debug("");
        logger.debug("-----------------------------------------------");
        logger.debug("");

        return newEndpoint;
    }
}

/*
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
 */
