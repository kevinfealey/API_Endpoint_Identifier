package com.aspectsecurity.automation.testing.JavaParser.Parsers;

import com.aspectsecurity.automation.testing.JavaParser.objects.AnnotationAttribute;

public class ConsumesParsers {

	public static String[] parseSpringRequestMappingConsumes(AnnotationAttribute attribute){
		String[] consumes = new String[10]; // capping at 10 ingest formats arbitrarily...
		consumes[0] = attribute.getValue().replaceAll("\"", "").trim(); //Stick whatever we have in an array - if it's just one consumes, we're good. If not, we'll replace the array contents later
		
		//remove array notation (ex. { item1, item2 }) and put each item in its own index in "consumes"
		if(attribute.getValue().trim().startsWith("{")){ //multiple "consumes" to add
			consumes = attribute.getValue().replaceAll("\\{", "").replaceAll("\\}", "").trim().split(","); 
		} 
		return consumes;
	}
}
