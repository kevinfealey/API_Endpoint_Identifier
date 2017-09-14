package com.aspectsecurity.automation.testing.JavaParser;

public class Parameter {

	private String name;
	private String value;
	private String type;
	
	public Parameter(String name, String value){
		this.name = name;
		this.value = value;
	}
	
	public Parameter() {
		// TODO Auto-generated constructor stub
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}
