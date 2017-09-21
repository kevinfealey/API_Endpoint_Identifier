package com.aspectsecurity.automation.testing.JavaParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectsecurity.automation.testing.JavaParser.objects.Endpoint;
import com.aspectsecurity.automation.testing.JavaParser.objects.Parameter;
import com.aspectsecurity.automation.testing.JavaParser.visitors.SpringAnnotationAnalyzer;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class SpringAPIIdentifier
{
    private static ArrayList<Endpoint> endpoints = new ArrayList<>();

    public static final String TEST_FILE_PATH = System.getProperty("user.dir") + "\\src\\test\\resources\\com\\aspectsecurity\\automation\\testing\\JavaParser\\test\\";

    public static void main(String[] args) throws IOException
    {
        Logger logger = LoggerFactory.getLogger(SpringAPIIdentifier.class);

        // Look in test dir by default, unless a path is passed
        String filePath = TEST_FILE_PATH;
        if (args.length > 0) {
            filePath = args[0];
        }

        // Find all files that end in .java
        ArrayList<Path> files = new ArrayList<Path>();
        Stream<Path> paths = Files.find(Paths.get(filePath), Integer.MAX_VALUE, (path,attrs) -> attrs.isRegularFile() && path.toString().endsWith(".java"));
        paths.forEach(files::add);
        paths.close();

        for (Path file : files) {
            logger.debug("Found file: " + file.toFile().toString());
            findEndpoints(file.toFile().toString());
        }
    }

    public static void findEndpoints(String file) throws FileNotFoundException
    {
        Logger logger = LoggerFactory.getLogger(SpringAPIIdentifier.class);

        // Assumes this file is part of this project
        FileInputStream in = new FileInputStream(file);

        // Parse it
        CompilationUnit cu = JavaParser.parse(in);

        // Visit and print the methods' names
        cu.accept(new SpringAnnotationAnalyzer(), cu.getPackageDeclaration());

        logger.debug("Printing Endpoint Info:");
        for (Endpoint endpoint : endpoints)
        {
            logger.info("=====================================================");
            logger.info("Name: " + (endpoint.getName() == null ? "<N/A>" : endpoint.getName()));
            logger.info("URL: " + endpoint.getUrl());
            logger.info("HTTP methods: " + endpoint.getMethods().toString());
            logger.info("Consumes: " + endpoint.getConsumes().toString());
            logger.info("Produces: " + endpoint.getProduces().toString());
            logger.info("Part of class: "+ endpoint.getClazzName());

            logger.info("Headers:");
            for (Parameter header : endpoint.getHeaders()){
                 logger.info("\t\t" + header.getHttpParameterName() + " = " + header.getDefaultValue());
            }

            logger.info("Parameters:");
            for (Parameter param : endpoint.getParams()){
                logger.info("\t\t" + param.getHttpParameterName() + " is a " + param.getAnnotation() + " of type " + param.getType() + " and a default value of " + (param.getDefaultValue().equals("") ? "<N/A>" : param.getDefaultValue())+ ". This input " + (param.isRequired() ? "is" : "is not") + " required.");
            }
        }
    }

    public static ArrayList<Endpoint> getEndpoints() {
        return endpoints;
    }

    public static void setEndpoints(ArrayList<Endpoint> endpoints) {
        SpringAPIIdentifier.endpoints = endpoints;
    }

    public static void addEndpoint(Endpoint endpoint)
    {
        // What do we do if we already have this endpoint? (ex. 2 endpoints with the same path are found)
        // Right now - end up with both. Is this the best in the long-run?
        SpringAPIIdentifier.endpoints.add(endpoint);
    }
}


