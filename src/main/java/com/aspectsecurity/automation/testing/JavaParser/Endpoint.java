package com.aspectsecurity.automation.testing.JavaParser;

import java.util.ArrayList;

public class Endpoint {

	private String url;
	private String method;
	private ArrayList<Parameter> headers;
	private ArrayList<Parameter> params;
	
	public Endpoint(String url, String method, Parameter ... parameter){
		this.url = url;
		this.method = method;
	
		for(Parameter param : parameter){
			params.add(param);
		}
	}
	
	public Endpoint() {
		// TODO Auto-generated constructor stub
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public ArrayList<Parameter> getParams() {
		return params;
	}
	public void setParams(ArrayList<Parameter> params) {
		this.params = params;
	}

	public ArrayList<Parameter> getHeaders() {
		return headers;
	}

	public void setHeaders(ArrayList<Parameter> headers) {
		this.headers = headers;
	}
	
	
	
}
