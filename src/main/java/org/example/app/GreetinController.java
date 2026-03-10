package org.example.app;

import org.example.annotations.GetMapping;
import org.example.annotations.RequestParam;
import org.example.annotations.RestController;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetinController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hola " + name;
    }
}