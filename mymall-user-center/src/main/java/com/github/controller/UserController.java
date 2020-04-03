package com.github.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @PostMapping("/info")
    public Object hello(@NotEmpty @Validated String name) {
        return  "Hello "+name;
    }
}
