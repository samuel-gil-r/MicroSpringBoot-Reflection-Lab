package org.example.app;

import org.example.annotations.GetMapping;
import org.example.annotations.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    static public String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping("/pi")
    static public String getPI() {
        return "PI = " + Math.PI;
    }

    @GetMapping("/hello")
    static public String hello() {
        return "Hello World!";
    }

}