package com.aspectsecurity.automation.testing.JavaParser.objects;

public class Parameter {
    private String httpParameterName;
    private String codeVariableName;
    private boolean required;

    private String defaultValue;
    private String type;
    private String annotation;

    public Parameter() {
    }

    public Parameter(String name, String value) {
        this.httpParameterName = name;
        this.defaultValue = value;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getHttpParameterName() {
        return httpParameterName;
    }

    public void setHttpParameterName(String name) {
        this.httpParameterName = name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String value) {
        this.defaultValue = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getCodeVariableName() {
        return codeVariableName;
    }

    public void setCodeVariableName(String codeVariableName) {
        this.codeVariableName = codeVariableName;
    }
}
