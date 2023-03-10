package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication(exclude = { DispatcherServletAutoConfiguration.class })
@ServletComponentScan
public class App {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(App.class, args);
  }

}