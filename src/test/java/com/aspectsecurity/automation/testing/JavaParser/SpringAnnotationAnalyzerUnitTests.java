package com.aspectsecurity.automation.testing.JavaParser;

import com.aspectsecurity.automation.testing.JavaParser.visitors.SpringAnnotationAnalyzer;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.type.VoidType;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class SpringAnnotationAnalyzerUnitTests {

    Logger logger;
    SpringAnnotationAnalyzer saa;

    @Before
    public void setUp() throws Exception {
        logger = LoggerFactory.getLogger(SpringAnnotationAnalyzerUnitTests.class);
        saa = new SpringAnnotationAnalyzer();
    }

    @Test
    public void testGetClassNameFromMethod() {
        //Code from: https://github.com/javaparser/javaparser/wiki/Manual
        CompilationUnit cu = new CompilationUnit();
        // set the package
        cu.setPackageDeclaration(new PackageDeclaration(Name.parse("com.aspectsecurity.example")));

        // or a shortcut
        cu.setPackageDeclaration("com.aspectsecurity.example");

        // create the type declaration
        ClassOrInterfaceDeclaration type = cu.addClass("GeneratedClass");

        // create a method
        EnumSet<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
        MethodDeclaration method = new MethodDeclaration(modifiers, new VoidType(), "main");
        modifiers.add(Modifier.STATIC);
        method.setModifiers(modifiers);
        type.addMember(method);

        assertEquals("GeneratedClass", saa.getClassNameFromMethod(method));
    }

}
