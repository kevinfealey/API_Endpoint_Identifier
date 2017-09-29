package com.aspectsecurity.automation.testing.JavaParser.parsers;

import com.aspectsecurity.automation.testing.JavaParser.objects.AnnotationAttribute;

public class ConsumesParsers {
    public static String[] parseSpringRequestMappingConsumes(AnnotationAttribute attribute) {
        // Capping at 10 ingest formats arbitrarily...
        String[] consumes = new String[10];
        // Stick whatever we have in an array - if it's just one consumes, we're good. If not, we'll replace the array contents later
        consumes[0] = attribute.getValue().replaceAll("\"", "").trim();

        // Remove array notation (ex. { item1, item2 }) and put each item in its own index in "consumes"
        if (attribute.getValue().trim().startsWith("{")) { // Multiple "consumes" to add
            consumes = attribute.getValue().replaceAll("\\{", "").replaceAll("\\}", "").trim().split(",");
        }

        return consumes;
    }
}
