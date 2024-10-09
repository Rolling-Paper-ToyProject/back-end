package com.sparklenote.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
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