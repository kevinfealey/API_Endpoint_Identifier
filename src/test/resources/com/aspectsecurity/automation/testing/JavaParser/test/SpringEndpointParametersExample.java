package com.aspectsecurity.automation.testing.JavaParser.test;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class SpringEndpointParametersExample {
    
	
    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return "Hi " + name;
    }
    
    @RequestMapping(path = "/endpoint11", headers = "key=val", method = {RequestMethod.POST, RequestMethod.GET,RequestMethod.PUT }, produces = "application/json")
    public String endpoint11( @RequestParam("id9") String id9, @RequestParam("id10") String id10){
    	return id9 + " and " + id10 + " returned.";
    }
    
    @RequestMapping("/endpoint12")
    public String index() {
        return "Greetings from Spring Boot!";
    }
    
    @RequestMapping(value = "/endpoint13/{id1}", method = RequestMethod.GET, consumes = {"application/json"})
    public String endpoint13( @PathVariable("id1") String id1){
    	return id1 + " returned.";
    }
     
    @RequestMapping("/endpoint15")
    public Greeting endpoint15(@RequestParam(value="name", defaultValue="World", required=true) String name) {
        return "Hi " + name;
    }
}