package com.aspectsecurity.automation.testing.JavaParser.objects;

public class AnnotationAttribute
{
	private String name;
	private String value;
	
	public AnnotationAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
