package com.aspectsecurity.automation.testing.JavaParser.objects;

import java.util.ArrayList;
import java.util.Collections;

public class Endpoint {
    private String name;
    private String url;
    private String clazzName;
    private ArrayList<String> consumes = new ArrayList<>();
    private ArrayList<String> produces = new ArrayList<>();
    private ArrayList<Parameter> headers = new ArrayList<>();
    private ArrayList<Parameter> params = new ArrayList<>();
    private ArrayList<String> methods = new ArrayList<>();

    public Endpoint() {
    }

    public Endpoint(String url, ArrayList<String> methods, Parameter... parameter) {
        this.url = url;
        this.methods = methods;

        Collections.addAll(params, parameter);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getProduces() {
        return produces;
    }

    public void setProduces(ArrayList<String> produces) {
        this.produces = produces;
    }

    public void addProduces(String produces) {
        this.produces.add(produces);
    }

    public ArrayList<String> getMethods() {
        return methods;
    }

    public void setMethods(ArrayList<String> methods) {
        this.methods = methods;
    }

    public void addMethod(String methods) {
        this.methods.add(methods);
    }

    public ArrayList<String> getConsumes() {
        return consumes;
    }

    public void setConsumes(ArrayList<String> consumes) {
        this.consumes = consumes;
    }

    public void addConsumes(String consumes) {
        this.consumes.add(consumes);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public void addHeaders(Parameter header) {
        this.headers.add(header);
    }

    public String getClazzName() {
        return clazzName;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }
}
