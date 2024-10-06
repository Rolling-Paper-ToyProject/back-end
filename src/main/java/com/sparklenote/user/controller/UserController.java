package com.sparklenote.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/user")
public class UserController {

    @GetMapping("/my")
    public String myAPI() {

        return "my route";
    }

    @GetMapping("/")
    public String mainAPI() {

        return "main route";
    }
}
