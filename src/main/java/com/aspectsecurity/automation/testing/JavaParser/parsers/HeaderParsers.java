package com.aspectsecurity.automation.testing.JavaParser.parsers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectsecurity.automation.testing.JavaParser.SpringAPIIdentifier;
import com.aspectsecurity.automation.testing.JavaParser.objects.AnnotationAttribute;
import com.aspectsecurity.automation.testing.JavaParser.objects.Parameter;

public class HeaderParsers {
    public static Parameter[] parseSpringRequestMappingHeaders(AnnotationAttribute attribute) {
        Logger logger = LoggerFactory.getLogger(SpringAPIIdentifier.class);

        // Capping at 10 headers arbitrarily...
        Parameter[] headers = new Parameter[10];

        // If there is no "=" something is probably wrong (header with a name, but no value), but we should handle it anyway
        if (!attribute.getValue().contains("=")) {
            logger.debug("Didn't find an \"=\" in header assignment. Weird.");
            headers[0] = new Parameter(attribute.getValue().replaceAll("\"", ""), "");
        } else {
            logger.debug("Found at least one header assignment.");
            //Stick whatever we have in an array - if it's just one header, we're good. If not, we'll replace the array contents later
            headers[0] = new Parameter(attribute.getValue().replaceAll("\"", "").trim().split("=")[0],
                    attribute.getValue().replaceAll("\"", "").trim().split("=")[1]);
            logger.debug("Attempting to add header: " + headers[0].getHttpParameterName() + " = " + headers[0].getDefaultValue());
        }

        // Remove array notation (ex. { item1=blah, item2=blah2 }) and put each item in its own index in "headers"
        if (attribute.getValue().trim().startsWith("{")) {
            // Multiple headers to add
            String[] tempArray = attribute.getValue().replaceAll("\\{", "").replaceAll("\\}", "").trim().split(",");

            for (int i = 0; i < tempArray.length; i++) {
                headers[i] = new Parameter(tempArray[i].split("=")[0].trim(), tempArray[i].split("=")[1].trim());
                logger.debug("Setting header: " + headers[i].getHttpParameterName() + " = " + headers[i].getDefaultValue());
            }
        }

        return headers;
    }
}
