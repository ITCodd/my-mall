package com.github.controller;


import cn.github.starter.auth.SecurityContextUtils;
import com.github.feign.UserFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/orders")
@Slf4j
public class OrderController {


//    @Autowired
//    private SecurityContextUtils securityContextUtils;

    @Autowired
    private UserFeignService userFeignService;

    @GetMapping("/t1")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String create(@AuthenticationPrincipal String username) {
        log.info("user is " + username);
        return username;
    }

    @GetMapping("/t2")
    /*@PreAuthorize("hasRole('ROLE_USER')")*/
    public Object t2() {
        String hello = userFeignService.hello(SecurityContextUtils.getAccessToken());
        System.out.println("hello = " + hello);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return  authentication.getPrincipal();
    }

    @GetMapping("/t3")
    public Object t3() {
        String hello = userFeignService.hello();
        System.out.println("hello = " + hello);
        return  "authentication.getPrincipal()";
    }

}
