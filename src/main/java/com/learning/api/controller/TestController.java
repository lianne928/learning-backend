package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@ApiController
@RequestMapping("/api")
public class TestController {
    @GetMapping("/test")
    public String test(){
        return "OK";
    }

    // test ok
    // http://localhost:8080/api/test
}
