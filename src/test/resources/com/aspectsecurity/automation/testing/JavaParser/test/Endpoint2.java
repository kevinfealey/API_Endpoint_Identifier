package com.aspectsecurity.automation.testing.JavaParser.test;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class Endpoint2 {
    
    @RequestMapping("/endpoint2")
    public String index() {
        return "Greetings from Spring Boot!";
    }
    
    @RequestMapping(value = "/endpoint3/{id}", method = RequestMethod.GET, consumes = {"application/json"})
    public String endpoint3( @PathVariable("id") String id){
    	return id + " returned.";
    }

    @RequestMapping(value = "/endpoint4", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public String endpoint4( @RequestParam("id") String id){
    	return id + " returned.";
    }
    
    @RequestMapping(value = "/endpoint5", method = RequestMethod.GET, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public String endpoint5( @RequestParam("id") String id, @RequestParam("id2") String id2){
    	return id + " and " + id2 + " returned.";
    }
 
    @RequestMapping(value = "/endpoint6", headers = { "key1=val1", "key2=val2" }, method = RequestMethod.POST, name = "endpoint6")
    public String endpoint6( @RequestParam("id") String id, @RequestParam("id2") String id2){
    	return id + " and " + id2 + " returned.";
    }    

    @RequestMapping(path = "/endpoint7", produces = { "application/json", "application/xml" }, headers = { "key1=val1", "key2=val2" }, method = {RequestMethod.PUT, RequestMethod.GET})
    public String endpoint7( @RequestParam("id") String id, @RequestParam("id2") String id2){
    	return id + " and " + id2 + " returned.";
    }
    
    @RequestMapping(path = "/endpoint8", headers = "key=val", method = {RequestMethod.POST, RequestMethod.GET,RequestMethod.PUT }, produces = "application/json")
    public String endpoint8( @RequestParam("id") String id, @RequestParam("id2") String id2){
    	return id + " and " + id2 + " returned.";
    }
}