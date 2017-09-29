package com.aspectsecurity.automation.testing.JavaParser.parsers;

import com.aspectsecurity.automation.testing.JavaParser.objects.AnnotationAttribute;

public class ProducesParsers {
    public static String[] parseSpringRequestMappingProduces(AnnotationAttribute attribute) {
        // Capping at 10 output formats arbitrarily...
        String[] produces = new String[10];

        // Stick whatever we have in an array - if it's just one produces, we're good. If not, we'll replace the array contents later
        produces[0] = attribute.getValue().replaceAll("\"", "").trim();

        // Remove array notation (ex. { item1, item2 }) and put each item in its own index in "produces"
        if (attribute.getValue().trim().startsWith("{")) {
            // multiple "produces" to add
            produces = attribute.getValue().replaceAll("\\{", "").replaceAll("\\}", "").trim().split(",");
        }

        return produces;
    }
}
