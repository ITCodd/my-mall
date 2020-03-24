package com.github.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/orders")
@Slf4j
public class OrderController {

    @GetMapping("/t1")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String create(@AuthenticationPrincipal String username) {
        log.info("user is " + username);
        return username;
    }

    @GetMapping("/t2")
    @PreAuthorize("hasRole('ROLE_USER')")
    public String t2() {
        return "ok";
    }

}
