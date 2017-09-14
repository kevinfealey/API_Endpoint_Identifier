package com.aspectsecurity.automation.testing.JavaParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectsecurity.automation.testing.JavaParser.objects.Endpoint;
import com.aspectsecurity.automation.testing.JavaParser.objects.Parameter;
import com.aspectsecurity.automation.testing.JavaParser.visitors.AnnotationAnalyzer;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;


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
		cu.accept(new AnnotationAnalyzer(), cu.getPackageDeclaration());

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
	
	public static ArrayList<Endpoint> getEndpoints() {
		return endpoints;
	}

	public static void setEndpoints(ArrayList<Endpoint> endpoints) {
		APIIdentifier.endpoints = endpoints;
	}
	
	public static void addEndpoint(Endpoint endpoint){
		APIIdentifier.endpoints.add(endpoint);
	}
}

	
