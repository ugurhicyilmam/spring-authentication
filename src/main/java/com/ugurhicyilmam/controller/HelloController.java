package com.ugurhicyilmam.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class HelloController {

    @RequestMapping(method = GET, value = "/hello")
    public String hello() {
        return "Hello World!";
    }
}
