package com.example.kafka;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {

  @GetMapping
  public String hello() {
    return "Hola";
  }

  @GetMapping("/thread")
  public String getThreadName() {
    return Thread.currentThread().toString();
  }
}
