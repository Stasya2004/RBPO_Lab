package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello World! *_* ";
    }

    @GetMapping("/test")
    public String test() {
        return "Test endpoint works! :3 ";
    }
}