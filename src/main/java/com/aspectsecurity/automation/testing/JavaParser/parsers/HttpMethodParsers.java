package com.aspectsecurity.automation.testing.JavaParser.parsers;

import com.aspectsecurity.automation.testing.JavaParser.objects.AnnotationAttribute;

public class HttpMethodParsers
{
	public static String[] parseSpringRequestMappingHttpMethods(AnnotationAttribute attribute)
    {
		String[] methods = new String[10]; // Capping at 10 methods arbitrarily...
		// Stick whatever we have in an array - if it's just one method, we're good. If not, we'll replace the array contents later
		methods[0] = attribute.getValue().replaceAll("\"", "").trim();
		
		if (attribute.getValue().trim().startsWith("{")) { // Multiple methods to add
			//remove array notation (ex. { item1, item2 }) and put each item in its own index in "methods"
			methods = attribute.getValue().replaceAll("\\{", "").replaceAll("\\}", "").trim().split(",");
		}

		return methods;
	}
}
