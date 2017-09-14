package com.aspectsecurity.automation.testing.JavaParser.Parsers;

import com.aspectsecurity.automation.testing.JavaParser.objects.AnnotationAttribute;

public class ProducesParsers {

	public static String[] parseSpringRequestMappingProduces(AnnotationAttribute attribute){
		String[] produces = new String[10]; // capping at 10 output formats arbitrarily...
		produces[0] = attribute.getValue().replaceAll("\"", "").trim(); //Stick whatever we have in an array - if it's just one produces, we're good. If not, we'll replace the array contents later
		
		//remove array notation (ex. { item1, item2 }) and put each item in its own index in "produces"
		if(attribute.getValue().trim().startsWith("{")){ //multiple "produces" to add
			produces = attribute.getValue().replaceAll("\\{", "").replaceAll("\\}", "").trim().split(","); 
		} 
		return produces;
	}
}
