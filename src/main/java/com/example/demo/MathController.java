package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MathController {

    @GetMapping("/add")
    public String addNumbers(@RequestParam int a, @RequestParam int b) {
        int result = a + b;
        return a + " + " + b + " = " + result;
    }

    @GetMapping("/multiply")
    public String multiplyNumbers(@RequestParam int a, @RequestParam int b) {
        int result = a * b;
        return a + " * " + b + " = " + result;
    }
}